/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  org.bukkit.Material
 *  org.bukkit.entity.Entity
 *  org.bukkit.entity.EntityType
 *  org.bukkit.entity.LivingEntity
 */
package kami.gg.souppvp.util.projectile.projectile;

import kami.gg.souppvp.util.projectile.TypedRunnable;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;

public interface CustomProjectile<T> {
    public EntityType getEntityType();

    public Entity getEntity();

    public LivingEntity getShooter();

    public String getProjectileName();

    public boolean isInvulnerable();

    public void setInvulnerable(boolean var1);

    public void addRunnable(Runnable var1);

    public void removeRunnable(Runnable var1);

    public void addTypedRunnable(TypedRunnable<T> var1);

    public void removeTypedRunnable(TypedRunnable<T> var1);

    public ArrayList<Material> getIgnoredBlocks();

    public int getKnockback();

    public void setKnockback(int var1);
}

