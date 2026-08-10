package com.gmail.nossr50.util.commands;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gmail.nossr50.datatypes.skills.PrimarySkillType;
import com.gmail.nossr50.locale.LocaleLoader;
import com.gmail.nossr50.mcMMO;
import java.util.List;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class CommandUtilsTest {

    private MockedStatic<mcMMO> mcMMOMock;
    private Player playerA;
    private Player playerB;

    @BeforeEach
    void setUp() {
        mcMMOMock = mockStatic(mcMMO.class);
        mcMMO.p = mock(mcMMO.class);
        final Server server = mock(Server.class);
        when(mcMMO.p.getServer()).thenReturn(server);

        playerA = mock(Player.class);
        when(playerA.getName()).thenReturn("PlayerA");
        playerB = mock(Player.class);
        when(playerB.getName()).thenReturn("PlayerB");
        when(server.getOnlinePlayers()).thenAnswer(invocation -> List.of(playerA, playerB));
    }

    @AfterEach
    void tearDown() {
        mcMMOMock.close();
    }

    /**
     * Regression coverage for console tab completion: the visibility check required a player
     * sender, so the console always got an empty player name list.
     */
    @Test
    void consoleShouldGetAllOnlinePlayerNames() {
        // Given - the command sender is the console
        final ConsoleCommandSender console = mock(ConsoleCommandSender.class);

        // When - online player names are collected for tab completion
        final List<String> names = CommandUtils.getOnlinePlayerNames(console);

        // Then - the console sees every online player
        assertThat(names).containsExactlyInAnyOrder("PlayerA", "PlayerB");
    }

    /** Guard: player senders must keep vanish support and only see players visible to them. */
    @Test
    void playersShouldOnlyGetNamesOfPlayersTheyCanSee() {
        // Given - a player who can see PlayerA but not the vanished PlayerB
        final Player viewer = mock(Player.class);
        when(viewer.canSee(playerA)).thenReturn(true);
        when(viewer.canSee(playerB)).thenReturn(false);

        // When - online player names are collected for tab completion
        final List<String> names = CommandUtils.getOnlinePlayerNames(viewer);

        // Then - only the visible player is listed
        assertThat(names).containsExactly("PlayerA");
    }

    @Test
    void isChildSkillShouldSendLocalizedMessageForChildSkill() {
        // Given - a child skill and LocaleLoader echoing keys
        final CommandSender sender = mock(CommandSender.class);
        try (final MockedStatic<LocaleLoader> localeLoader = mockStatic(LocaleLoader.class)) {
            localeLoader.when(() -> LocaleLoader.getString(anyString()))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // When - the skill is checked
            final boolean result = CommandUtils.isChildSkill(sender, PrimarySkillType.SALVAGE);

            // Then - the sender is notified with the correct locale key
            assertThat(result).isTrue();
            verify(sender).sendMessage("Commands.Skill.ChildSkill");
        }
    }

    @Test
    void isChildSkillShouldNotMessageForNonChildSkill() {
        // Given - a primary (non-child) skill
        final CommandSender sender = mock(CommandSender.class);
        try (final MockedStatic<LocaleLoader> localeLoader = mockStatic(LocaleLoader.class)) {
            localeLoader.when(() -> LocaleLoader.getString(anyString()))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // When - the skill is checked
            final boolean result = CommandUtils.isChildSkill(sender, PrimarySkillType.MINING);

            // Then - no message is sent
            assertThat(result).isFalse();
            verify(sender, never()).sendMessage(anyString());
        }
    }

    @Test
    void isInvalidIntegerShouldSendLocalizedMessageForNonInteger() {
        // Given - a non-integer argument
        final CommandSender sender = mock(CommandSender.class);
        try (final MockedStatic<LocaleLoader> localeLoader = mockStatic(LocaleLoader.class)) {
            localeLoader.when(() -> LocaleLoader.getString(anyString()))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // When - the value is validated
            final boolean result = CommandUtils.isInvalidInteger(sender, "abc");

            // Then - the sender is notified with the correct locale key
            assertThat(result).isTrue();
            verify(sender).sendMessage("Commands.Invalid.Integer");
        }
    }

    @Test
    void isInvalidIntegerShouldReturnFalseForValidInteger() {
        // Given - a valid integer string
        final CommandSender sender = mock(CommandSender.class);
        try (final MockedStatic<LocaleLoader> localeLoader = mockStatic(LocaleLoader.class)) {
            localeLoader.when(() -> LocaleLoader.getString(anyString()))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // When - the value is validated
            final boolean result = CommandUtils.isInvalidInteger(sender, "42");

            // Then - validation passes without messaging the sender
            assertThat(result).isFalse();
            verify(sender, never()).sendMessage(anyString());
        }
    }

    @Test
    void isInvalidDoubleShouldSendLocalizedMessageForNonDouble() {
        // Given - a non-double argument
        final CommandSender sender = mock(CommandSender.class);
        try (final MockedStatic<LocaleLoader> localeLoader = mockStatic(LocaleLoader.class)) {
            localeLoader.when(() -> LocaleLoader.getString(anyString()))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // When - the value is validated
            final boolean result = CommandUtils.isInvalidDouble(sender, "abc");

            // Then - the sender is notified with the correct locale key
            assertThat(result).isTrue();
            verify(sender).sendMessage("Commands.Invalid.Double");
        }
    }

    @Test
    void isInvalidDoubleShouldReturnFalseForValidDouble() {
        // Given - a valid double string
        final CommandSender sender = mock(CommandSender.class);
        try (final MockedStatic<LocaleLoader> localeLoader = mockStatic(LocaleLoader.class)) {
            localeLoader.when(() -> LocaleLoader.getString(anyString()))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // When - the value is validated
            final boolean result = CommandUtils.isInvalidDouble(sender, "3.14");

            // Then - validation passes without messaging the sender
            assertThat(result).isFalse();
            verify(sender, never()).sendMessage(anyString());
        }
    }
}
