package kami.gg.souppvp.util.projectile.projectile;

import kami.gg.souppvp.util.projectile.TypedRunnable;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;

/** Stub — original NMS impl removed (unused at runtime). */
public class OrbProjectile implements CustomProjectile<OrbProjectile> {
    private final String name;
    private final LivingEntity shooter;
    private final ArrayList<Material> ignored = new ArrayList<>();
    private int knockback;

    public OrbProjectile(String name, Location loc, LivingEntity shooter, float power) {
        this.name = name; this.shooter = shooter;
    }
    public OrbProjectile(String name, LivingEntity shooter, float power) { this(name, shooter.getLocation(), shooter, power); }

    @Override public EntityType getEntityType() { return EntityType.EXPERIENCE_ORB; }
    @Override public Entity getEntity() { return shooter; }
    @Override public LivingEntity getShooter() { return shooter; }
    @Override public String getProjectileName() { return name; }
    @Override public boolean isInvulnerable() { return false; }
    @Override public void setInvulnerable(boolean v) {}
    @Override public void addRunnable(Runnable r) {}
    @Override public void removeRunnable(Runnable r) {}
    @Override public void addTypedRunnable(TypedRunnable<OrbProjectile> r) {}
    @Override public void removeTypedRunnable(TypedRunnable<OrbProjectile> r) {}
    @Override public ArrayList<Material> getIgnoredBlocks() { return ignored; }
    @Override public int getKnockback() { return knockback; }
    @Override public void setKnockback(int i) { this.knockback = i; }
}
