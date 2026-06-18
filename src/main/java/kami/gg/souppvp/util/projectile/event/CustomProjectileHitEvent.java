/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  org.bukkit.block.Block
 *  org.bukkit.block.BlockFace
 *  org.bukkit.entity.EntityType
 *  org.bukkit.entity.LivingEntity
 *  org.bukkit.event.Cancellable
 *  org.bukkit.event.Event
 *  org.bukkit.event.HandlerList
 */
package kami.gg.souppvp.util.projectile.event;

import kami.gg.souppvp.util.projectile.projectile.CustomProjectile;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CustomProjectileHitEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final CustomProjectile projectile;
    private final float damageMultiplier;
    private LivingEntity entity;
    private Block block;
    private BlockFace face;
    private boolean cancelled;

    public CustomProjectileHitEvent(CustomProjectile pro, float damageMultiplier, LivingEntity ent) {
        this.projectile = pro;
        this.entity = ent;
        this.damageMultiplier = damageMultiplier;
    }

    public CustomProjectileHitEvent(CustomProjectile pro, float damageMultiplier, Block b, BlockFace f) {
        this.projectile = pro;
        this.block = b;
        this.face = f;
        this.damageMultiplier = damageMultiplier;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public CustomProjectile getProjectile() {
        return this.projectile;
    }

    public float getDamageMultiplier() {
        return this.damageMultiplier;
    }

    public LivingEntity getHitEntity() {
        return this.entity;
    }

    public Block getHitBlock() {
        return this.block;
    }

    public BlockFace getHitFace() {
        return this.face;
    }

    public HitType getHitType() {
        if (this.block == null && this.entity != null) {
            return HitType.ENTITY;
        }
        if (this.block != null && this.entity == null && this.face != null) {
            return HitType.BLOCK;
        }
        return null;
    }

    public EntityType getProjectileType() {
        return this.getProjectile().getEntityType();
    }

    public String toString() {
        if (this.getHitType() == HitType.ENTITY) {
            return "{" + ((Object)((Object)this)).getClass().getName() + " projectile:" + this.projectile.toString() + ", hit entity:" + this.entity.toString() + "}";
        }
        if (this.getHitType() == HitType.BLOCK) {
            return "{" + ((Object)((Object)this)).getClass().getName() + " projectile:" + this.projectile.toString() + ", hit block:" + this.block.toString() + ", face hit:" + this.face.toString() + "}";
        }
        return ((Object)((Object)this)).getClass().getName();
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    public void setCancelled(boolean value) {
        this.cancelled = value;
    }

    public static enum HitType {
        ENTITY,
        BLOCK;

    }
}

