//package kr.rtustudio.donation.bukkit.command;
//
//import kr.rtustudio.donation.bukkit.BukkitDonationAPI;
//import kr.rtustudio.donation.common.data.Platform;
//import kr.rtustudio.donation.common.data.Response;
//import kr.rtustudio.donation.common.data.Service;
//import kr.rtustudio.framework.bukkit.api.command.RSCommand;
//import kr.rtustudio.framework.bukkit.api.command.RSCommandData;
//import kr.rtustudio.framework.bukkit.api.scheduler.CraftScheduler;
//
//import java.util.Arrays;
//import java.util.List;
//import java.util.concurrent.CompletableFuture;
//
//public class UnregisterCommand extends RSCommand<BukkitDonationAPI> {
//
//    public UnregisterCommand(BukkitDonationAPI plugin) {
//        super(plugin, "unregister");
//    }
//
//    @Override
//    public RSCommand.Result execute(RSCommandData data) {
//        String serviceArg = data.args(1);
//        String platformArg = data.args(2);
//        String user = data.args(3);
//
//        if (serviceArg.isEmpty() || platformArg.isEmpty() || user.isEmpty()) return RSCommand.Result.FAILURE;
//
//        Service service = parseService(serviceArg);
//        Platform platform = parsePlatform(platformArg);
//        if (service == null || platform == null) return RSCommand.Result.FAILURE;
//
//        CompletableFuture<Response> result;
//        if (!service.getPlatforms().contains(platform)) {
//            result = CompletableFuture.completedFuture(Response.UNSUPPORTED);
//        } else {
//            result = getPlugin().getDonationAPI().unregister(platform, user);
//        }
//        result.thenAccept(r -> CraftScheduler.sync(() -> {
//            chat().announce(message().get(player(), messageKey(r)));
//        }));
//        return RSCommand.Result.SUCCESS;
//    }
//
//    @Override
//    public List<String> tabComplete(RSCommandData data) {
//        if (data.length(1)) return Arrays.stream(Service.values())
//                .map(this::displayName).toList();
//        if (data.length(2)) {
//            try {
//                Service service = parseService(data.args(1));
//                if (service == null) return List.of();
//                return service.getPlatforms().stream().map(Platform::lowercase).toList();
//            } catch (IllegalArgumentException ignored) {
//                return List.of();
//            }
//        }
//        return List.of();
//    }
//
//    private Service parseService(String value) {
//        if (value == null || value.isEmpty()) return null;
//        String plain = value.replace("-", "").replace("_", "");
//        String normalized = plain.toUpperCase();
//        return switch (normalized) {
//            case "SSAPI" -> Service.SSAPI;
//            case "CHZZKOFFICIAL" -> Service.CHZZK_OFFICIAL;
//            default -> plain.equalsIgnoreCase("치지직") ? Service.CHZZK_OFFICIAL : null;
//        };
//    }
//
//    private Platform parsePlatform(String value) {
//        if (value == null || value.isEmpty()) return null;
//        try {
//            return Platform.valueOf(value.toUpperCase());
//        } catch (IllegalArgumentException ex) {
//            return null;
//        }
//    }
//
//    private String displayName(Service service) {
//        return switch (service) {
//            case SSAPI -> "ssapi";
//            case CHZZK_OFFICIAL -> "치지직";
//        };
//    }
//
//    private String messageKey(Response response) {
//        return switch (response) {
//            case Response.SUCCESS -> "unregister.success";
//            case Response.UNSUPPORTED -> "unregister.unsupported";
//            default -> "unregister.fail";
//        };
//    }
//
//}
