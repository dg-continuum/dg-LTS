package kr.syeyoung.dungeonsguide.dungeon.actions

import kr.syeyoung.dungeonsguide.dungeon.DungeonRoom

class ActionTree {
    lateinit var parent: MutableSet<ActionTree>
    lateinit var current: AbstractAction
    lateinit var children: MutableSet<ActionTree>

    override fun hashCode(): Int {
        return current.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as ActionTree
        return parent == that.parent && current == that.current && children == that.children
    }

    companion object {
        @JvmStatic
        fun buildActionTree(actions: AbstractAction, dungeonRoom: DungeonRoom): ActionTree? {
            return buildActionTree(null, actions, dungeonRoom, HashMap())
        }

        @JvmStatic
        fun buildActionTree(
            parent: ActionTree?,
            action: AbstractAction,
            dungeonRoom: DungeonRoom,
            alreadyBuilt: MutableMap<AbstractAction, ActionTree>
        ): ActionTree? {
            if (alreadyBuilt.containsKey(action)) {
                val tree = alreadyBuilt[action]
                if (parent != null) {
                    tree?.parent?.add(parent)
                }
                return tree
            }
            val tree = ActionTree()
            alreadyBuilt[action] = tree
            tree.parent = HashSet()
            if (parent != null) {
                (tree.parent as HashSet<ActionTree>).add(parent)
            }
            tree.current = action
            val set = HashSet<ActionTree>()
            val preRequisites = action.getPreRequisites(dungeonRoom)
            for (action2 in preRequisites) {
                val e = buildActionTree(tree, action2, dungeonRoom, alreadyBuilt)
                if (e != null) {
                    set.add(e)
                }
            }
            tree.children = set
            return tree
        }
    }
}

fun linearityActionTree(input: ActionTree): List<AbstractAction> {
    val tree = copyActionTree(input)
    val actions: MutableList<AbstractAction> = ArrayList()
    var plsHalt = 0
    while (tree.children.size != 0) {
        plsHalt++
        check(plsHalt <= 1000000) { "Linearifying process ran for 1 million cycle" }
        val visited: MutableSet<ActionTree> = HashSet()
        var curr = tree
        var plsHalt2 = 0
        while (curr.children.size != 0) {
            plsHalt2++
            check(plsHalt2 <= 1000000) { "Finding the leaf of tree ran for 1 million cycles" }
            check(!visited.contains(curr)) { "Circular Reference Detected" }
            visited.add(curr)
            curr = curr.children.iterator().next()
        }
        plsHalt2 = 0
        while (curr.children.size == 0) {
            plsHalt2++
            check(plsHalt2 <= 1000000) { "Building of array ran for 1 million cycles" }
            actions.add(curr.current)
            if (curr.parent.size == 0) break
            for (parentTree in curr.parent) parentTree.children.remove(curr)
            curr = curr.parent.iterator().next()
        }
    }
    return actions
}

fun copyActionTree(tree: ActionTree): ActionTree {
    val built: MutableMap<ActionTree, ActionTree> = HashMap()
    require(tree.parent.size == 0) { "that is not head of tree" }
    return copyActionTree(tree, built)!!
}

fun copyActionTree(tree: ActionTree, preBuilts: MutableMap<ActionTree, ActionTree>): ActionTree? {
    if (preBuilts.containsKey(tree)) return preBuilts[tree]
    val clone = ActionTree()
    preBuilts[tree] = clone
    clone.current = tree.current
    clone.parent = HashSet()
    clone.children = HashSet()
    for (tree3 in tree.children) {
        val clone3 = copyActionTree(tree3, preBuilts)
        clone3!!.parent.add(clone)
        clone.children.add(clone3)
    }
    return clone
}