package kami.gg.souppvp.util.assemble;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.Collections;
import java.util.List;

public class AssembleThread extends BukkitRunnable {

    private final Assemble assemble;

    /**
     * Assemble Thread.
     *
     * @param assemble instance.
     */
    AssembleThread(Assemble assemble) {
        this.assemble = assemble;
        // Runs on the main thread — safe to touch Bukkit API and plugin data structures.
        this.runTaskTimer(assemble.getPlugin(), 0L, assemble.getTicks());
    }

    @Override
    public void run() {
        for (Player player : this.assemble.getPlugin().getServer().getOnlinePlayers()) {
            try {
                AssembleBoard board = this.assemble.getBoards().get(player.getUniqueId());

                if (board == null) {
                    continue;
                }

                Scoreboard scoreboard = board.getScoreboard();
                Objective objective = board.getObjective();

                if (scoreboard == null || objective == null) {
                    continue;
                }

                String title = LegacyComponentSerializer.legacySection()
                        .serialize(this.assemble.getAdapter().getTitle(player));

                if (!objective.getDisplayName().equals(title)) {
                    objective.setDisplayName(title);
                }

                List<Component> newLines = this.assemble.getAdapter().getLines(player);

                if (newLines == null || newLines.isEmpty()) {
                    board.getEntries().forEach(AssembleBoardEntry::remove);
                    board.getEntries().clear();
                } else {
                    if (newLines.size() > 15) {
                        newLines = newLines.subList(0, 15);
                    }

                    if (!this.assemble.getAssembleStyle().isDescending()) {
                        Collections.reverse(newLines);
                    }

                    if (board.getEntries().size() > newLines.size()) {
                        for (int i = newLines.size(); i < board.getEntries().size(); i++) {
                            AssembleBoardEntry entry = board.getEntryAtPosition(i);
                            if (entry != null) {
                                entry.remove();
                            }
                        }
                    }

                    int cache = this.assemble.getAssembleStyle().getStartNumber();
                    for (int i = 0; i < newLines.size(); i++) {
                        AssembleBoardEntry entry = board.getEntryAtPosition(i);

                        String line = LegacyComponentSerializer.legacySection()
                                .serialize(newLines.get(i));

                        if (entry == null) {
                            entry = new AssembleBoardEntry(board, line, i);
                        }

                        entry.setText(line);
                        entry.setup();
                        entry.send(
                                this.assemble.getAssembleStyle().isDescending() ? cache-- : cache++
                        );
                    }
                }

                if (player.getScoreboard() != scoreboard && !assemble.isHook()) {
                    player.setScoreboard(scoreboard);
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new AssembleException("There was an error updating " + player.getName() + "'s scoreboard.");
            }
        }
    }
}