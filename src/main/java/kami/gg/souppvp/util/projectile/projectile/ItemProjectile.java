package kami.gg.souppvp.util.projectile.projectile;

import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.util.projectile.TypedRunnable;
import kami.gg.souppvp.util.projectile.event.ItemProjectileHitEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Modern 1.21.8 reimplementation of the legacy NMS-based ItemProjectile.
 * Spawns a dropped {@link Item} entity with velocity, repeatedly ticks
 * runnables until it hits a block, hits a LivingEntity, or expires.
 */
public class ItemProjectile implements CustomProjectile<ItemProjectile> {

    private final String name;
    private final LivingEntity shooter;
    private final ItemStack stack;
    private final Item entity;
    private int knockback;
    private final ArrayList<Material> ignoredMaterials = new ArrayList<>();
    private final List<Runnable> runnables = new ArrayList<>();
    private final List<TypedRunnable<ItemProjectile>> typedRunnables = new ArrayList<>();
    private int age;
    private boolean dead;

    public ItemProjectile(String name, Location loc, ItemStack stack, LivingEntity shooter, float power) {
        this.name = name;
        this.shooter = shooter;
        this.stack = stack.clone();
        this.entity = loc.getWorld().dropItem(loc, this.stack);
        this.entity.setPickupDelay(Integer.MAX_VALUE);
        this.entity.setVelocity(loc.getDirection().multiply(power * 1.5));
        start();
    }

    public ItemProjectile(String name, LivingEntity shooter, ItemStack stack, float power) {
        this(name, shooter.getEyeLocation(), stack, shooter, power);
    }

    private void start() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (dead || !entity.isValid() || entity.isDead()) {
                    if (entity.isValid()) entity.remove();
                    cancel();
                    return;
                }
                age++;
                if (age > 200) { // 10 seconds at 20tps
                    die();
                    cancel();
                    return;
                }
                // Hit detection — nearby living entities (other than shooter, within 1 block).
                for (Entity nearby : entity.getNearbyEntities(1.0, 1.0, 1.0)) {
                    if (nearby instanceof LivingEntity && !nearby.equals(shooter)) {
                        LivingEntity victim = (LivingEntity) nearby;
                        ItemProjectileHitEvent ev = new ItemProjectileHitEvent(ItemProjectile.this, 1.0f, victim, stack);
                        Bukkit.getPluginManager().callEvent(ev);
                        if (!ev.isCancelled()) {
                            if (knockback > 0) {
                                Vector v = entity.getVelocity().normalize().multiply(knockback * 0.6);
                                victim.setVelocity(v.setY(0.1));
                            }
                            die();
                            cancel();
                            return;
                        }
                    }
                }
                // Block collision — when the item entity stops moving meaningfully.
                if (entity.isOnGround()) {
                    ItemProjectileHitEvent ev = new ItemProjectileHitEvent(ItemProjectile.this, 1.0f,
                            entity.getLocation().getBlock(), org.bukkit.block.BlockFace.UP, stack);
                    Bukkit.getPluginManager().callEvent(ev);
                    if (!ev.isCancelled()) {
                        die();
                        cancel();
                        return;
                    }
                }
                for (Runnable r : runnables) r.run();
                for (TypedRunnable<ItemProjectile> r : typedRunnables) r.run(ItemProjectile.this);
            }
        }.runTaskTimer(SoupPvP.getInstance(), 1L, 1L);
    }

    public void die() {
        if (dead) return;
        dead = true;
        if (entity.isValid()) entity.remove();
    }

    public ItemStack getItem() { return stack; }
    @Override public EntityType getEntityType() { return EntityType.ITEM; }
    @Override public Entity getEntity() { return entity; }
    @Override public LivingEntity getShooter() { return shooter; }
    @Override public String getProjectileName() { return name; }
    @Override public boolean isInvulnerable() { return entity.isInvulnerable(); }
    @Override public void setInvulnerable(boolean value) { entity.setInvulnerable(value); }
    @Override public void addRunnable(Runnable r) { runnables.add(r); }
    @Override public void removeRunnable(Runnable r) { runnables.remove(r); }
    @Override public void addTypedRunnable(TypedRunnable<ItemProjectile> r) { typedRunnables.add(r); }
    @Override public void removeTypedRunnable(TypedRunnable<ItemProjectile> r) { typedRunnables.remove(r); }
    @Override public ArrayList<Material> getIgnoredBlocks() { return ignoredMaterials; }
    @Override public int getKnockback() { return knockback; }
    @Override public void setKnockback(int i) { this.knockback = i; }
}
