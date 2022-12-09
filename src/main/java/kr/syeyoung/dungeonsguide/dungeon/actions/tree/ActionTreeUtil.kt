/*
 *     Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 *     Copyright (C) 2021  cyoung06
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package kr.syeyoung.dungeonsguide.dungeon.actions.tree

import kr.syeyoung.dungeonsguide.dungeon.actions.AbstractAction

object ActionTreeUtil {
    @JvmStatic
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

    private fun copyActionTree(tree: ActionTree): ActionTree {
        val built: MutableMap<ActionTree, ActionTree> = HashMap()
        require(tree.parent.size == 0) { "that is not head of tree" }
        return copyActionTree(tree, built)!!
    }

    private fun copyActionTree(tree: ActionTree, preBuilts: MutableMap<ActionTree, ActionTree>): ActionTree? {
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
}