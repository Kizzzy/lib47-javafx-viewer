package cn.kizzzy.javafx.viewer;

import cn.kizzzy.javafx.common.MenuItemArg;
import cn.kizzzy.javafx.display.DisplayTabView;
import cn.kizzzy.vfs.IPackage;
import cn.kizzzy.vfs.tree.Leaf;
import cn.kizzzy.vfs.tree.Node;

public interface ViewerExecutor {
    
    void initialize(ViewerExecutorArgs args);
    
    void stop(ViewerExecutorArgs args);
    
    void initOperator(DisplayTabView tabView, IPackage vfs);
    
    void displayLeaf(ViewerExecutorArgs args, Leaf leaf);
    
    Iterable<MenuItemArg> showContext(ViewerExecutorArgs args, Node selected);
}
