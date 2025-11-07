package kr.rtustudio.donation.bukkit.command;

import kr.rtustudio.donation.bukkit.BukkitDonationAPI;
import kr.rtustudio.donation.common.data.RoomInfo;
import kr.rtustudio.donation.common.data.RoomUser;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import kr.rtustudio.framework.bukkit.api.command.RSCommandData;
import kr.rtustudio.framework.bukkit.api.scheduler.CraftScheduler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import java.util.concurrent.CompletableFuture;

public class InfoCommand extends RSCommand<BukkitDonationAPI> {

    public InfoCommand(BukkitDonationAPI plugin) {
        super(plugin, "info");
    }

    @Override
    public Result execute(RSCommandData data) {
        chat().announce(message().get(player(), "info.querying"));
        CompletableFuture<RoomInfo> result = getPlugin().getAPI().info();
        result.thenAccept(info -> CraftScheduler.sync(() -> sendInfo(info)));
        return Result.SUCCESS;
    }

    private void sendInfo(RoomInfo info) {
        if (info == null) {
            chat().announce(message().get(player(), "info.empty"));
            return;
        }
        int usersCount = info.users() != null ? info.users().size() : 0;

        chat().announce(message().get(player(), "info.header-id")
                .replace("{id}", String.valueOf(info.id())));
        chat().announce(message().get(player(), "info.header-count")
                .replace("{count}", String.valueOf(usersCount))
                .replace("{limit}", String.valueOf(info.usersLimit())));
        chat().announce(message().get(player(), "info.header-created")
                .replace("{createdAt}", String.valueOf(info.createdAt())));
        chat().announce(message().get(player(), "info.header-updated")
                .replace("{updatedAt}", String.valueOf(info.updatedAt())));

        if (usersCount == 0) {
            chat().announce(message().get(player(), "info.users-empty"));
            return;
        }
        sendUserList(info);
    }

    private void sendUserList(RoomInfo info) {
        chat().announce(message().get(player(), "info.users-title"));
        for (int i = 0; i < info.users().size(); i++) {
            RoomUser u = info.users().get(i);
            chat().send(buildUserLine(u));
        }
    }

    private Component buildUserLine(RoomUser u) {
        String created = u.createdAt();
        String createdSuffix = (created != null && !created.isEmpty())
                ? message().get(player(), "info.user-created-suffix").replace("{createdAt}", created)
                : "";
        String platform = String.valueOf(u.platform());
        String streamerId = String.valueOf(u.streamerId());

        Component nameComp = Component.text(streamerId)
                .clickEvent(ClickEvent.copyToClipboard(streamerId))
                .hoverEvent(HoverEvent.showText(Component.text("클릭하여 복사")));
        if (platform.equalsIgnoreCase("chzzk")) nameComp = nameComp.color(NamedTextColor.GREEN);
        if (platform.equalsIgnoreCase("soop")) nameComp = nameComp.color(NamedTextColor.AQUA);

        Component lineComp = Component.text(" - ").append(nameComp);
        if (!createdSuffix.isEmpty()) lineComp = lineComp.append(Component.text(createdSuffix));
        return lineComp;
    }

}
