package cn.kizzzy.javafx.viewer;

import cn.kizzzy.vfs.IPackage;
import cn.kizzzy.vfs.tree.Node;
import javafx.scene.control.TreeItem;

public class ViewerExecutorBinder {
    
    private final IPackage vfs;
    
    private final ViewerExecutor executor;
    
    private final TreeItem<Node> parent;
    
    public ViewerExecutorBinder(IPackage vfs, ViewerExecutor executor) {
        this(vfs, executor, null);
    }
    
    public ViewerExecutorBinder(IPackage vfs, ViewerExecutor executor, TreeItem<Node> parent) {
        this.vfs = vfs;
        this.executor = executor;
        this.parent = parent;
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
    
    public TreeItem<Node> getParent() {
        return parent;
    }
}
