/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  org.bukkit.block.Block
 *  org.bukkit.block.BlockFace
 *  org.bukkit.entity.LivingEntity
 *  org.bukkit.inventory.ItemStack
 */
package kami.gg.souppvp.util.projectile.event;

import kami.gg.souppvp.util.projectile.projectile.CustomProjectile;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

public class ItemProjectileHitEvent
extends CustomProjectileHitEvent {
    private final ItemStack item;

    public ItemProjectileHitEvent(CustomProjectile pro, float damageMultiplier, Block b, BlockFace f, ItemStack item) {
        super(pro, damageMultiplier, b, f);
        this.item = item;
    }

    public ItemProjectileHitEvent(CustomProjectile pro, float damageMultiplier, LivingEntity ent, ItemStack item) {
        super(pro, damageMultiplier, ent);
        this.item = item;
    }

    public ItemStack getItemStack() {
        return this.item;
    }
}

