package cn.kizzzy.javafx.viewer;

import cn.kizzzy.vfs.IPackage;
import cn.kizzzy.vfs.tree.Node;

public class ViewerExecutorBinder {
    
    private final IPackage vfs;
    
    private final ViewerExecutor executor;
    
    public ViewerExecutorBinder(IPackage vfs, ViewerExecutor executor) {
        this.vfs = vfs;
        this.executor = executor;
    }
    
    public boolean contains(Node node) {
        return vfs.getNode(node.id) != null;
    }
    
    public IPackage getVfs() {
        return vfs;
    }
    
    public ViewerExecutor getExecutor() {
        return executor;
    }
}
