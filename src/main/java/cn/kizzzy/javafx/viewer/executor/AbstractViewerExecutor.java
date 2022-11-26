package cn.kizzzy.javafx.viewer.executor;

import cn.kizzzy.helper.LogHelper;
import cn.kizzzy.helper.StringHelper;
import cn.kizzzy.javafx.StageHelper;
import cn.kizzzy.javafx.display.DisplayOperator;
import cn.kizzzy.javafx.setting.SettingDialog;
import cn.kizzzy.javafx.viewer.ViewerExecutor;
import cn.kizzzy.javafx.viewer.ViewerExecutorArgs;
import cn.kizzzy.vfs.tree.Leaf;
import cn.kizzzy.vfs.tree.Node;
import javafx.stage.Stage;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.File;

public abstract class AbstractViewerExecutor implements ViewerExecutor {
    
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
                LogHelper.error("open folder error: " + path, e);
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
}
