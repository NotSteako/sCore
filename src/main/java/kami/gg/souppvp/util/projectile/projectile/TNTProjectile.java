package kami.gg.souppvp.util.projectile.projectile;

import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.util.projectile.TypedRunnable;
import kami.gg.souppvp.util.projectile.event.CustomProjectileHitEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * 1.21.8 reimplementation of the legacy NMS-based TNTProjectile.
 * Spawns a Bukkit {@link TNTPrimed} entity, throws it like a projectile, and
 * runs a per-tick collision loop. On collision (entity hit, block hit, or fuse
 * expiry) we fire {@link CustomProjectileHitEvent} so existing kit / perk code
 * keeps working unchanged.
 */
public class TNTProjectile implements CustomProjectile<TNTProjectile> {

    private final String name;
    private final LivingEntity shooter;
    private final TNTPrimed entity;
    private int knockback;
    private int age;
    private boolean dead;
    private final ArrayList<Material> ignoredMaterials = new ArrayList<>();
    private final List<Runnable> runnables = new ArrayList<>();
    private final List<TypedRunnable<TNTProjectile>> typedRunnables = new ArrayList<>();

    public TNTProjectile(String name, Location loc, LivingEntity shooter, float power) {
        this.name = name;
        this.shooter = shooter;
        this.entity = (TNTPrimed) loc.getWorld().spawnEntity(loc, EntityType.TNT);
        try { this.entity.setSource(shooter); } catch (Throwable ignored) {}
        this.entity.setFuseTicks(20);
        Vector dir = loc.getDirection().multiply(power * 1.5f);
        this.entity.setVelocity(dir);
        start();
    }

    public TNTProjectile(String name, LivingEntity shooter, float power) {
        this(name, shooter.getEyeLocation(), shooter, power);
    }

    private void start() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (dead) {
                    cancel();
                    return;
                }
                age++;
                if (age > 1000 || !entity.isValid() || entity.isDead()) {
                    die();
                    cancel();
                    return;
                }

                // Block collision check at current position (excluding air/ignored materials)
                Location loc = entity.getLocation();
                Material atBlock = loc.getBlock().getType();
                if (!atBlock.isAir() && !ignoredMaterials.contains(atBlock) && loc.getBlock().getBoundingBox().contains(loc.toVector())) {
                    float damageMultiplier = (float) entity.getVelocity().length();
                    CustomProjectileHitEvent ev = new CustomProjectileHitEvent(TNTProjectile.this, damageMultiplier, loc.getBlock(), BlockFace.UP);
                    Bukkit.getPluginManager().callEvent(ev);
                    if (!ev.isCancelled()) {
                        die();
                        cancel();
                        return;
                    }
                }

                // Entity collision check — anything living within 1.3 blocks (besides the shooter for first 5 ticks).
                for (Entity nearby : entity.getNearbyEntities(1.3, 1.3, 1.3)) {
                    if (!(nearby instanceof LivingEntity)) continue;
                    if (nearby.equals(shooter) && age < 5) continue;
                    LivingEntity victim = (LivingEntity) nearby;
                    float damageMultiplier = (float) entity.getVelocity().length();
                    CustomProjectileHitEvent ev = new CustomProjectileHitEvent(TNTProjectile.this, damageMultiplier, victim);
                    Bukkit.getPluginManager().callEvent(ev);
                    if (!ev.isCancelled()) {
                        if (knockback > 0) {
                            Vector v = entity.getVelocity().clone().normalize().multiply(knockback * 0.6);
                            victim.setVelocity(v.setY(0.1));
                        }
                        die();
                        cancel();
                        return;
                    }
                }

                for (Runnable r : runnables) r.run();
                for (TypedRunnable<TNTProjectile> r : typedRunnables) r.run(TNTProjectile.this);
            }
        }.runTaskTimer(SoupPvP.getInstance(), 1L, 1L);
    }

    /** Cancel the projectile and explode at its current location. */
    public void die() {
        if (dead) return;
        dead = true;
        if (entity.isValid()) {
            Location loc = entity.getLocation();
            entity.remove();
            // Explode at the current location with default 4.0 power, no fire, breaks blocks.
            loc.getWorld().createExplosion(loc, 4.0F, false, true, shooter);
        }
    }

    @Override public EntityType getEntityType() { return EntityType.TNT; }
    @Override public Entity getEntity() { return entity; }
    @Override public LivingEntity getShooter() { return shooter; }
    @Override public String getProjectileName() { return name; }
    @Override public boolean isInvulnerable() { return entity.isInvulnerable(); }
    @Override public void setInvulnerable(boolean value) { entity.setInvulnerable(value); }
    @Override public void addRunnable(Runnable r) { runnables.add(r); }
    @Override public void removeRunnable(Runnable r) { runnables.remove(r); }
    @Override public void addTypedRunnable(TypedRunnable<TNTProjectile> r) { typedRunnables.add(r); }
    @Override public void removeTypedRunnable(TypedRunnable<TNTProjectile> r) { typedRunnables.remove(r); }
    @Override public ArrayList<Material> getIgnoredBlocks() { return ignoredMaterials; }
    @Override public int getKnockback() { return knockback; }
    @Override public void setKnockback(int i) { this.knockback = i; }
}
