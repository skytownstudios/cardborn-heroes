package com.skytownstudios.cardbornheroes.data

import android.content.Context
import org.json.JSONObject

data class RigPivot(val x: Float, val y: Float)

data class RigAttackAnim(
    val arm: String,
    val rotationRest: Float,
    val rotationSwing: Float
)

data class RoleRigDef(
    val layerOrder: List<String>,
    val armLeftPivot: RigPivot,
    val armRightPivot: RigPivot,
    val unarmedAttack: RigAttackAnim,
    val melee1hAttack: RigAttackAnim,
    val dualOffhandTilt: Float
)

data class RigManifest(
    val canvasSize: Int,
    val mainGripX: Int,
    val mainGripY: Int,
    val offGripX: Int,
    val offGripY: Int,
    val roles: Map<String, RoleRigDef>
) {
    fun role(rigId: String): RoleRigDef? = roles[rigId]

    companion object {
        fun load(context: Context): RigManifest {
            val json = context.assets.open("config/rig_manifest.json").bufferedReader().use { it.readText() }
            return parse(JSONObject(json))
        }

        fun parse(root: JSONObject): RigManifest {
            val anchors = root.getJSONObject("weaponAnchors")
            val main = anchors.getJSONObject("mainHand")
            val off = anchors.getJSONObject("offHand")
            val rolesJson = root.getJSONObject("roles")
            val roles = buildMap {
                rolesJson.keys().forEach { roleId ->
                    val r = rolesJson.getJSONObject(roleId)
                    val pivots = r.getJSONObject("pivots")
                    val left = pivots.getJSONObject("arm_left")
                    val right = pivots.getJSONObject("arm_right")
                    val anims = r.getJSONObject("animations")
                    val unarmed = anims.getJSONObject("unarmed_attack")
                    val melee = anims.getJSONObject("melee_1h_attack")
                    put(
                        roleId,
                        RoleRigDef(
                            layerOrder = r.getJSONArray("layerOrder").let { arr ->
                                (0 until arr.length()).map { arr.getString(it) }
                            },
                            armLeftPivot = RigPivot(left.getDouble("x").toFloat(), left.getDouble("y").toFloat()),
                            armRightPivot = RigPivot(right.getDouble("x").toFloat(), right.getDouble("y").toFloat()),
                            unarmedAttack = RigAttackAnim(
                                arm = unarmed.getString("arm"),
                                rotationRest = unarmed.getDouble("rotationRest").toFloat(),
                                rotationSwing = unarmed.getDouble("rotationSwing").toFloat()
                            ),
                            melee1hAttack = RigAttackAnim(
                                arm = melee.getString("arm"),
                                rotationRest = melee.getDouble("rotationRest").toFloat(),
                                rotationSwing = melee.getDouble("rotationSwing").toFloat()
                            ),
                            dualOffhandTilt = anims.optDouble("dual_1h_offhand_tilt", 8.0).toFloat()
                        )
                    )
                }
            }
            return RigManifest(
                canvasSize = root.getInt("canvasSize"),
                mainGripX = main.getInt("gripX"),
                mainGripY = main.getInt("gripY"),
                offGripX = off.getInt("gripX"),
                offGripY = off.getInt("gripY"),
                roles = roles
            )
        }
    }
}
