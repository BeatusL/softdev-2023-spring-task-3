package com.github.BeatusL.mlnk.system

import com.badlogic.gdx.math.Rectangle
import com.github.BeatusL.mlnk.component.AttackComponent
import com.github.BeatusL.mlnk.component.AttackState
import com.github.BeatusL.mlnk.component.PhysicsComponent
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.badlogic.gdx.physics.box2d.World
import com.github.BeatusL.mlnk.component.LifeComponent
import com.github.BeatusL.mlnk.system.EntitySpawnSystem.Companion.HITBOX
import ktx.box2d.query
import ktx.math.component1
import ktx.math.component2

@AllOf([AttackComponent::class, PhysicsComponent::class])
class AttackSystem (
    private val attacCmps: ComponentMapper<AttackComponent>,
    private val physicsCmps: ComponentMapper<PhysicsComponent>,
    private val lifeCmps: ComponentMapper<LifeComponent>,
    private val oWorld: World,
): IteratingSystem() {



    override fun onTickEntity(entity: Entity) {
        val attackCmp = attacCmps[entity]
        val physCmp = physicsCmps[entity]

        if (attackCmp.state == AttackState.DEALING_DMG) {
            val (x, y) = physCmp.body.position
            val (width, height) = physCmp.size

            if (y !in -1f..17f) lifeCmps[entity].takeDamage += attackCmp.damage


            rect.set(x - width/4f, y - height/4f, x + width/4f, y + height/4f) // hitboxes are meant to be smaller than actual models

            oWorld.query(rect.x, rect.y, rect.width, rect.height) {fixture ->
                if (fixture.userData != HITBOX) {
                    return@query true
                }
                val fixtureEntity = fixture.entity
                if (fixtureEntity == entity || attackCmp.friendly == attacCmps[fixtureEntity].friendly)
                    return@query true

                configureEntity(fixtureEntity) {
                    lifeCmps.getOrNull(it)?. let { lifeCmp -> lifeCmp.takeDamage += attackCmp.damage}
                }
                lifeCmps[entity].takeDamage += attackCmp.damage
                return@query false
            }
        }

    }

    companion object {
        val rect = Rectangle()
    }
}
