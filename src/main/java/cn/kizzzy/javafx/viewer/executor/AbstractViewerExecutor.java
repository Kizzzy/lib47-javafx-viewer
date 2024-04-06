package cn.kizzzy.javafx.viewer.executor;

import cn.kizzzy.helper.StringHelper;
import cn.kizzzy.javafx.StageHelper;
import cn.kizzzy.javafx.display.DisplayOperator;
import cn.kizzzy.javafx.setting.SettingDialog;
import cn.kizzzy.javafx.viewer.ViewerExecutor;
import cn.kizzzy.javafx.viewer.ViewerExecutorArgs;
import cn.kizzzy.vfs.tree.Leaf;
import cn.kizzzy.vfs.tree.Node;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class AbstractViewerExecutor implements ViewerExecutor {
    
    protected static final Logger logger = LoggerFactory.getLogger(AbstractViewerExecutor.class);
    
    protected DisplayOperator displayer;
    
    @Override
    public void stop(ViewerExecutorArgs args) {
        displayer.stop();
    }
    
    @Override
    public void displayLeaf(ViewerExecutorArgs args, Leaf leaf) {
        displayer.display(args.getVfs(), leaf);
    }
    
    protected void openSetting(ViewerExecutorArgs args, Object config) {
        Stage stage = args.getStage();
        StageHelper stageHelper = args.getStageHelper();
        
        SettingDialog.Args _args = new SettingDialog.Args();
        _args.target = config;
        
        stageHelper.show(stage, _args, SettingDialog.class);
    }
    
    protected void openFolderImpl(String path) {
        new Thread(() -> {
            try {
                if (StringHelper.isNotNullAndEmpty(path)) {
                    Desktop.getDesktop().open(new File(path));
                }
            } catch (Exception e) {
                logger.error("open folder error: " + path, e);
            }
        }).start();
    }
    
    protected void copyPath(Node selected) {
        if (selected != null && selected.leaf) {
            String path = ((Leaf) selected).path.replace("\\", "\\\\");
            StringSelection selection = new StringSelection(path);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
        }
    }
    
    protected File showOpenFolder(ViewerExecutorArgs args, String title, Supplier<String> getter, Consumer<String> setter) {
        Stage stage = args.getStage();
        
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle(title);
        if (StringHelper.isNotNullAndEmpty(getter.get())) {
            File folder = new File(getter.get());
            if (folder.exists()) {
                chooser.setInitialDirectory(folder);
            }
        }
        
        File file = chooser.showDialog(stage);
        if (file != null) {
            setter.accept(file.getAbsolutePath());
        }
        
        return file;
    }
    
    protected File showOpenFile(ViewerExecutorArgs args, String title, Supplier<String> getter, Consumer<String> setter) {
        Stage stage = args.getStage();
        
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        if (StringHelper.isNotNullAndEmpty(getter.get())) {
            File lastFolder = new File(getter.get());
            if (lastFolder.exists()) {
                chooser.setInitialDirectory(lastFolder);
            }
        }
        
        File file = chooser.showOpenDialog(stage);
        if (file != null) {
            setter.accept(file.getParent());
        }
        
        return file;
    }
    
    protected File showSaveFile(ViewerExecutorArgs args, String title, Supplier<String> getter, Consumer<String> setter) {
        Stage stage = args.getStage();
        
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        if (StringHelper.isNotNullAndEmpty(getter.get())) {
            File lastFolder = new File(getter.get());
            if (lastFolder.exists()) {
                chooser.setInitialDirectory(lastFolder);
            }
        }
        
        File file = chooser.showSaveDialog(stage);
        if (file != null) {
            setter.accept(file.getParent());
        }
        
        return file;
    }
}
