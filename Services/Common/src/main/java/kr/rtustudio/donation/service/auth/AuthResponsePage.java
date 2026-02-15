package kr.rtustudio.donation.service.auth;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class AuthResponsePage {

    private AuthResponsePage() {}

    public static @NotNull String success(@NotNull String serviceName) {
        return buildHtml(serviceName, true, "연동이 완료되었습니다!", "이 창을 닫고 마인크래프트로 돌아가주세요.", null);
    }

    public static @NotNull String success(@NotNull String serviceName, @NotNull String notice) {
        return buildHtml(serviceName, true, "연동이 완료되었습니다!", "이 창을 닫고 마인크래프트로 돌아가주세요.", notice);
    }

    public static @NotNull String failure(@NotNull String serviceName, @NotNull String reason) {
        return buildHtml(serviceName, false, "연동에 실패했습니다.", reason, null);
    }

    private static String buildHtml(@NotNull String serviceName, boolean success, @NotNull String title, @NotNull String description, @Nullable String notice) {
        String color = success ? "#22c55e" : "#ef4444";
        String bgColor = success ? "#f0fdf4" : "#fef2f2";
        String icon = success ? "&#10003;" : "&#10007;";
        String iconBg = success ? "#dcfce7" : "#fee2e2";
        String noticeHtml = notice != null
                ? "<p class=\"notice\">⚠ " + notice + "</p>"
                : "";

        return """
                <!DOCTYPE html>
                <html lang="ko">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>%s - DonationAPI</title>
                    <style>
                        * { margin: 0; padding: 0; box-sizing: border-box; }
                        body {
                            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                            background: %s;
                            display: flex;
                            justify-content: center;
                            align-items: center;
                            min-height: 100vh;
                        }
                        .card {
                            background: white;
                            border-radius: 16px;
                            padding: 48px;
                            text-align: center;
                            box-shadow: 0 4px 24px rgba(0, 0, 0, 0.08);
                            max-width: 420px;
                            width: 90%%;
                        }
                        .icon {
                            width: 72px;
                            height: 72px;
                            border-radius: 50%%;
                            background: %s;
                            color: %s;
                            font-size: 36px;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            margin: 0 auto 24px;
                        }
                        .service {
                            font-size: 13px;
                            font-weight: 600;
                            color: %s;
                            text-transform: uppercase;
                            letter-spacing: 1px;
                            margin-bottom: 8px;
                        }
                        h1 {
                            font-size: 22px;
                            color: #1a1a1a;
                            margin-bottom: 12px;
                        }
                        p {
                            font-size: 15px;
                            color: #6b7280;
                            line-height: 1.5;
                        }
                        .notice {
                            font-size: 14px;
                            color: #d97706;
                            background: #fffbeb;
                            border: 1px solid #fde68a;
                            border-radius: 8px;
                            padding: 12px 16px;
                            margin-top: 16px;
                            line-height: 1.5;
                        }
                    </style>
                </head>
                <body>
                    <div class="card">
                        <div class="icon">%s</div>
                        <div class="service">%s</div>
                        <h1>%s</h1>
                        <p>%s</p>
                        %s
                    </div>
                </body>
                </html>
                """.formatted(serviceName, bgColor, iconBg, color, color, icon, serviceName, title, description, noticeHtml);
    }

}
