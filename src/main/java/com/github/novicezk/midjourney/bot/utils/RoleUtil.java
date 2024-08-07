package com.github.novicezk.midjourney.bot.utils;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import java.util.List;
import java.util.stream.Collectors;

public class RoleUtil {
    public static List<Member> getMembersWithRole(Guild guild, List<Member> members, String roleId) {
        Role role = guild.getRoleById(roleId);

        if (role == null) {
            System.out.println("Role not found.");
            return List.of();
        }

        return members.stream()
                .filter(member -> member.getRoles().contains(role))
                .collect(Collectors.toList());
    }
}
