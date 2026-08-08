package com.gmail.nossr50.datatypes.party;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

class PartyVisibilityTest {

    @Test
    void createMembersListShowsVanishedMembersAsOffline() {
        UUID leaderUuid = new UUID(1, 1);
        UUID visibleUuid = new UUID(2, 2);
        UUID vanishedUuid = new UUID(3, 3);

        PartyLeader leader = new PartyLeader(leaderUuid, "Leader");
        Party party = new Party(leader, "TestParty");
        party.getMembers().put(leaderUuid, "Leader");
        party.getMembers().put(visibleUuid, "Visible");
        party.getMembers().put(vanishedUuid, "Vanished");

        Player viewer = mock(Player.class);
        Player visibleMember = mock(Player.class);
        Player vanishedMember = mock(Player.class);

        when(viewer.getUniqueId()).thenReturn(leaderUuid);
        when(visibleMember.getUniqueId()).thenReturn(visibleUuid);
        when(vanishedMember.getUniqueId()).thenReturn(vanishedUuid);

        when(viewer.canSee(viewer)).thenReturn(true);
        when(viewer.canSee(visibleMember)).thenReturn(true);
        when(viewer.canSee(vanishedMember)).thenReturn(false);

        party.addOnlineMember(viewer);
        party.addOnlineMember(visibleMember);
        party.addOnlineMember(vanishedMember);

        String memberList = party.createMembersList(viewer);

        assertThat(memberList).contains(ChatColor.GOLD + "Leader");
        assertThat(memberList).contains(ChatColor.GREEN + "Visible");
        assertThat(memberList).contains(ChatColor.DARK_GRAY + "Vanished");
        assertThat(memberList).doesNotContain(ChatColor.GREEN + "Vanished");
    }

    @Test
    void getVisibleMembersExcludesVanishedPlayers() {
        UUID leaderUuid = new UUID(1, 1);
        UUID visibleUuid = new UUID(2, 2);
        UUID vanishedUuid = new UUID(3, 3);

        PartyLeader leader = new PartyLeader(leaderUuid, "Leader");
        Party party = new Party(leader, "TestParty");

        Player viewer = mock(Player.class);
        Player visibleMember = mock(Player.class);
        Player vanishedMember = mock(Player.class);

        when(viewer.getUniqueId()).thenReturn(leaderUuid);
        when(visibleMember.getUniqueId()).thenReturn(visibleUuid);
        when(vanishedMember.getUniqueId()).thenReturn(vanishedUuid);

        when(viewer.canSee(viewer)).thenReturn(true);
        when(viewer.canSee(visibleMember)).thenReturn(true);
        when(viewer.canSee(vanishedMember)).thenReturn(false);

        party.addOnlineMember(viewer);
        party.addOnlineMember(visibleMember);
        party.addOnlineMember(vanishedMember);

        List<Player> visibleMembers = party.getVisibleMembers(viewer);

        assertThat(visibleMembers).containsExactlyInAnyOrder(viewer, visibleMember);
        assertThat(visibleMembers).doesNotContain(vanishedMember);
    }
}
