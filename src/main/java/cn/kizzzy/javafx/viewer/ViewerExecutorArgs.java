package cn.kizzzy.javafx.viewer;

import cn.kizzzy.javafx.StageHelper;
import cn.kizzzy.observe.value.ValueObservable;
import cn.kizzzy.vfs.IPackage;
import cn.kizzzy.vfs.tree.IdGenerator;
import javafx.stage.Stage;

public interface ViewerExecutorArgs {
    
    Stage getStage();
    
    IPackage getUserVfs();
    
    StageHelper getStageHelper();
    
    IPackage getVfs();
    
    IdGenerator getIdGenerator();
    
    ValueObservable<ViewerExecutorBinder> getObservable();
}
