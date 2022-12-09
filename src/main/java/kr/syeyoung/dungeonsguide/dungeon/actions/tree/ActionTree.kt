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

import kr.syeyoung.dungeonsguide.dungeon.DungeonRoom
import kr.syeyoung.dungeonsguide.dungeon.actions.AbstractAction

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