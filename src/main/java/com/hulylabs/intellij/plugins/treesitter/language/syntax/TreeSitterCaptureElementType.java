package com.hulylabs.intellij.plugins.treesitter.language.syntax;

import com.hulylabs.intellij.plugins.treesitter.language.TreeSitterLanguage;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.ArrayFactory;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class TreeSitterCaptureElementType extends IElementType {
    private static short registrySize = 0; // guarded by lock
    private static volatile TreeSitterCaptureElementType[] registry = new TreeSitterCaptureElementType[0]; // writes guarded by lock
    private static final HashMap<String, TreeSitterCaptureElementType> registryMap = new HashMap<>(); // guarded by lock
    private static final Object lock = new String("registryLock");
    private static final TreeSitterCaptureElementType[] EMPTY_ARRAY = new TreeSitterCaptureElementType[0];
    private static final ArrayFactory<TreeSitterCaptureElementType> ARRAY_FACTORY = count -> count == 0 ? EMPTY_ARRAY : new TreeSitterCaptureElementType[count];
    public static final TreeSitterCaptureElementType NONE = findOrCreate("none");


    private final @NotNull String groupName;
    private final short groupId;

    private TreeSitterCaptureElementType(@NotNull String groupName) {
        super("TREE_SITTER_CAPTURE_" + groupName, TreeSitterLanguage.INSTANCE);
        synchronized (lock) {
            groupId = registrySize++;
            registryMap.put(groupName, this);
            TreeSitterCaptureElementType[] newRegistry = registrySize > registry.length ? ArrayUtil.realloc(registry, registry.length * 3 / 2 + 1, ARRAY_FACTORY) : registry;
            newRegistry[groupId] = this;
            registry = newRegistry;
        }
        this.groupName = groupName;
    }

    public static TreeSitterCaptureElementType findOrCreate(String groupName) {
        TreeSitterCaptureElementType elementType;
        synchronized (lock) {
            elementType = registryMap.get(groupName);
        }
        if (elementType == null) {
            elementType = new TreeSitterCaptureElementType(groupName);
        }
        return elementType;
    }

    public final short getGroupId() {
        return groupId;
    }

    public final @NotNull String getGroupName() {
        return groupName;
    }

    public static TreeSitterCaptureElementType find(int groupId) {
        // volatile read, registry does not shrink
        return registry[groupId];
    }
}
