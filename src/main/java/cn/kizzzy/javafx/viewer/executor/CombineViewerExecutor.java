package cn.kizzzy.javafx.viewer.executor;

import cn.kizzzy.javafx.common.MenuItemArg;
import cn.kizzzy.javafx.display.DisplayTabView;
import cn.kizzzy.javafx.viewer.ViewerExecutor;
import cn.kizzzy.javafx.viewer.ViewerExecutorArgs;
import cn.kizzzy.javafx.viewer.ViewerExecutorBinder;
import cn.kizzzy.observe.value.ValueObserveArgs;
import cn.kizzzy.vfs.IPackage;
import cn.kizzzy.vfs.tree.Leaf;
import cn.kizzzy.vfs.tree.Node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CombineViewerExecutor implements ViewerExecutor {
    
    private final List<ViewerExecutor> executors;
    
    private final List<ViewerExecutorBinder> binders;
    
    public CombineViewerExecutor(ViewerExecutor... executors) {
        this(Arrays.asList(executors));
    }
    
    public CombineViewerExecutor(Iterable<ViewerExecutor> executors) {
        this.executors = new ArrayList<>();
        for (ViewerExecutor executor : executors) {
            this.executors.add(executor);
        }
        binders = new ArrayList<>();
    }
    
    @Override
    public void initialize(ViewerExecutorArgs args) {
        for (ViewerExecutor executor : executors) {
            executor.initialize(args);
        }
        args.getObservable().add(this::onPackageChanged);
    }
    
    @Override
    public void stop(ViewerExecutorArgs args) {
        for (ViewerExecutor executor : executors) {
            executor.stop(args);
        }
    }
    
    @Override
    public void initOperator(DisplayTabView tabView, IPackage vfs) {
        for (ViewerExecutor executor : executors) {
            executor.initOperator(tabView, vfs);
        }
    }
    
    @Override
    public void displayLeaf(ViewerExecutorArgs args, Leaf leaf) {
        for (ViewerExecutorBinder binder : binders) {
            if (binder.contains(leaf)) {
                binder.getExecutor().displayLeaf(args, leaf);
                return;
            }
        }
    }
    
    @Override
    public Iterable<MenuItemArg> showContext(ViewerExecutorArgs args, Node selected) {
        List<MenuItemArg> list = new ArrayList<>();
        
        ViewerExecutorBinder _binder = null;
        if (selected != null) {
            for (ViewerExecutorBinder binder : binders) {
                if (binder.contains(selected)) {
                    _binder = binder;
                }
            }
        }
        
        for (ViewerExecutor executor : executors) {
            Node _target = _binder == null || _binder.getExecutor() != executor ? null : selected;
            for (MenuItemArg arg : executor.showContext(args, _target)) {
                list.add(arg);
            }
        }
        
        return list;
    }
    
    private void onPackageChanged(ValueObserveArgs<ViewerExecutorBinder> args) {
        ViewerExecutorBinder binder = args.getNewValue();
        if (binder != null) {
            binders.add(binder);
        }
    }
}
