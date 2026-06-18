package kami.gg.souppvp.util.projectile.projectile;

import kami.gg.souppvp.util.projectile.TypedRunnable;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;

/** Stub — original NMS impl removed (unused at runtime). */
public class ProjectileScheduler implements Runnable, CustomProjectile<ProjectileScheduler> {
    private final String name;
    private final Entity entity;
    private final LivingEntity shooter;
    private final ArrayList<Material> ignored = new ArrayList<>();
    private int knockback;

    public ProjectileScheduler(String name, Entity entity, LivingEntity shooter, float power, Plugin plugin) {
        this.name = name; this.entity = entity; this.shooter = shooter;
    }

    @Override public void run() {}
    @Override public EntityType getEntityType() { return entity == null ? EntityType.ITEM : entity.getType(); }
    @Override public Entity getEntity() { return entity; }
    @Override public LivingEntity getShooter() { return shooter; }
    @Override public String getProjectileName() { return name; }
    @Override public boolean isInvulnerable() { return false; }
    @Override public void setInvulnerable(boolean v) {}
    @Override public void addRunnable(Runnable r) {}
    @Override public void removeRunnable(Runnable r) {}
    @Override public void addTypedRunnable(TypedRunnable<ProjectileScheduler> r) {}
    @Override public void removeTypedRunnable(TypedRunnable<ProjectileScheduler> r) {}
    @Override public ArrayList<Material> getIgnoredBlocks() { return ignored; }
    @Override public int getKnockback() { return knockback; }
    @Override public void setKnockback(int i) { this.knockback = i; }
}
