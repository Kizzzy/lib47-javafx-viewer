package cn.kizzzy.toolkit.controller;

import cn.kizzzy.helper.StringHelper;
import cn.kizzzy.javafx.JavafxControlParameter;
import cn.kizzzy.javafx.StageHelper;
import cn.kizzzy.javafx.common.JavafxHelper;
import cn.kizzzy.javafx.display.DisplayTabView;
import cn.kizzzy.javafx.plugin.PluginView;
import cn.kizzzy.javafx.setting.SettingDialog;
import cn.kizzzy.javafx.viewer.ViewerExecutor;
import cn.kizzzy.javafx.viewer.ViewerExecutorArgs;
import cn.kizzzy.javafx.viewer.ViewerExecutorBinder;
import cn.kizzzy.observe.value.ValueObservable;
import cn.kizzzy.observe.value.ValueObserveArgs;
import cn.kizzzy.vfs.IPackage;
import cn.kizzzy.vfs.pack.CombinePackage;
import cn.kizzzy.vfs.pack.FilePackage;
import cn.kizzzy.vfs.tree.IdGenerator;
import cn.kizzzy.vfs.tree.Leaf;
import cn.kizzzy.vfs.tree.Node;
import cn.kizzzy.vfs.tree.NodeComparator;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

abstract class ExplorerViewBase extends PluginView {
    
    @FXML
    protected TextField filterValue;
    
    @FXML
    protected TreeView<Node> tree_view;
    
    @FXML
    protected DisplayTabView display_tab;
    
    @FXML
    protected Label tips;
}

@JavafxControlParameter(fxml = "/fxml/viewer/explorer_view.fxml")
public abstract class ExplorerView extends ExplorerViewBase implements Initializable {
    
    protected static final Comparator<TreeItem<Node>> comparator
        = Comparator.comparing(TreeItem<Node>::getValue, new NodeComparator());
    
    protected IPackage userVfs;
    
    protected StageHelper stageHelper;
    
    protected CombinePackage vfs;
    protected IdGenerator idGenerator;
    
    protected ValueObservable<ViewerExecutorBinder> observable;
    
    protected ViewerExecutor executor;
    
    protected TreeItem<Node> dummyRoot;
    protected TreeItem<Node> filterRoot;
    
    protected Map<Integer, IPackage> childVfsKvs;
    
    protected ViewerExecutorArgs args;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        childVfsKvs = new HashMap<>();
        args = new ViewerExecutorArgs() {
            
            @Override
            public Stage getStage() {
                return stage;
            }
            
            @Override
            public IPackage getUserVfs() {
                return userVfs;
            }
            
            @Override
            public StageHelper getStageHelper() {
                return stageHelper;
            }
            
            @Override
            public IPackage getVfs() {
                return vfs;
            }
            
            @Override
            public IdGenerator getIdGenerator() {
                return idGenerator;
            }
            
            @Override
            public ValueObservable<ViewerExecutorBinder> getObservable() {
                return observable;
            }
        };
        
        userVfs = new FilePackage(System.getProperty("user.home") + "/.user");
        
        stageHelper = new StageHelper();
        stageHelper.addFactory(SettingDialog::new, SettingDialog.class);
        
        vfs = new CombinePackage();
        idGenerator = new IdGenerator();
        
        observable = new ValueObservable<>();
        observable.add(this::onPackageChanged);
        
        executor = initialViewExecutor();
        executor.initialize(args);
        executor.initOperator(display_tab, vfs);
        
        dummyRoot = new TreeItem<>();
        tree_view.setRoot(dummyRoot);
        tree_view.setShowRoot(false);
        tree_view.getSelectionModel().selectedItemProperty().addListener(this::onSelectItem);
        
        JavafxHelper.initContextMenu(tree_view, () -> {
            TreeItem<Node> selected = tree_view.getSelectionModel().getSelectedItem();
            return executor.showContext(args, selected, selected == null ? null : selected.getValue());
        });
    }
    
    protected abstract ViewerExecutor initialViewExecutor();
    
    @Override
    public void stop() {
        if (vfs != null) {
            vfs.stop();
        }
        
        executor.stop(args);
        
        super.stop();
    }
    
    @FXML
    protected void onFilter(ActionEvent event) {
        final String regex = filterValue.getText();
        if (StringHelper.isNullOrEmpty(regex)) {
            return;
        }
        
        try {
            Pattern.compile(regex);
        } catch (Exception e) {
            return;
        }
        
        if (filterRoot == null) {
            filterRoot = new TreeItem<>(new Node(-1, "[Filter]"));
            dummyRoot.getChildren().add(filterRoot);
        }
        
        filterRoot.getChildren().clear();
        
        List<Node> list = vfs.listNodeByRegex(regex);
        for (Node folder : list) {
            TreeItem<Node> treeItem = new TreeItem<>(folder);
            filterRoot.getChildren().add(treeItem);
        }
        
        filterRoot.getChildren().sort(comparator);
    }
    
    protected void onSelectItem(Observable observable, TreeItem<Node> oldValue, TreeItem<Node> newValue) {
        Node node = newValue == null ? null : newValue.getValue();
        if (node != null && node.id >= 0) {
            if (node.leaf) {
                IPackage childVfs = childVfsKvs.get(node.id);
                if (childVfs == null) {
                    Leaf leaf = (Leaf) node;
                    executor.displayLeaf(args, leaf);
                } else {
                    for (Node root : childVfs.listNode(0)) {
                        buildNodeImpl(newValue, childVfs, root.id);
                    }
                }
            } else {
                buildNodeImpl(newValue, vfs, node.id);
            }
        }
    }
    
    private void buildNodeImpl(TreeItem<Node> treeItem, IPackage vfs, int nodeId) {
        treeItem.getChildren().clear();
        
        Iterable<Node> nodes = vfs.listNode(nodeId, false);
        for (Node temp : nodes) {
            TreeItem<Node> child = new TreeItem<>(temp);
            treeItem.getChildren().add(child);
        }
        treeItem.getChildren().sort(comparator);
    }
    
    private void onPackageChanged(ValueObserveArgs<ViewerExecutorBinder> args) {
        IPackage _vfs = args.getNewValue().getVfs();
        TreeItem<Node> parent = args.getNewValue().getParent();
        if (_vfs != null) {
            vfs.addPackage(_vfs);
            
            if (parent != null && parent != dummyRoot) {
                childVfsKvs.put(parent.getValue().id, _vfs);
            }
            
            doAfterLoadVfs(parent == null ? vfs : _vfs, parent == null ? dummyRoot : parent);
        }
    }
    
    private void doAfterLoadVfs(IPackage vfs, TreeItem<Node> parent) {
        Platform.runLater(() -> {
            parent.getChildren().clear();
            
            if (parent == dummyRoot) {
                for (Node root : vfs.listNode(0)) {
                    parent.getChildren().add(new TreeItem<>(root));
                }
                
                if (filterRoot != null) {
                    filterRoot.getChildren().clear();
                    
                    parent.getChildren().add(filterRoot);
                }
            } else {
                for (Node root : vfs.listNode(0)) {
                    for (Node child : root.children.values()) {
                        parent.getChildren().add(new TreeItem<>(child));
                    }
                }
            }
        });
    }
}