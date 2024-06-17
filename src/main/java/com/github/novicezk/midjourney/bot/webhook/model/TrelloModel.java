package com.github.novicezk.midjourney.bot.webhook.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class TrelloModel {
    private Model model;
    private Action action;
    private Webhook webhook;

    @Data
    public static class Model {
        private String id;
        private String name;
        private String desc;
        private DescData descData;
        private boolean closed;
        private String idOrganization;
        private String idEnterprise;
        private boolean pinned;
        private String url;
        private String shortUrl;
        private Prefs prefs;
        private Map<String, String> labelNames;

        @Data
        public static class DescData {
            private Map<String, Object> emoji;
        }

        @Data
        public static class Prefs {
            private String permissionLevel;
            private boolean hideVotes;
            private String voting;
            private String comments;
            private String invitations;
            private boolean selfJoin;
            private boolean cardCovers;
            private boolean cardCounts;
            private boolean isTemplate;
            private String cardAging;
            private boolean calendarFeedEnabled;
            private List<String> hiddenPluginBoardButtons;
            private List<SwitcherView> switcherViews;
            private String background;
            private String backgroundColor;
            private String backgroundImage;
            private boolean backgroundTile;
            private String backgroundBrightness;
            private String sharedSourceUrl;
            private List<BackgroundImageScaled> backgroundImageScaled;
            private String backgroundBottomColor;
            private String backgroundTopColor;
            private boolean canBePublic;
            private boolean canBeEnterprise;
            private boolean canBeOrg;
            private boolean canBePrivate;
            private boolean canInvite;

            @Data
            public static class SwitcherView {
                private String viewType;
                private boolean enabled;
            }

            @Data
            public static class BackgroundImageScaled {
                private int width;
                private int height;
                private String url;
            }
        }
    }

    @Data
    public static class Action {
        private String id;
        private String idMemberCreator;
        private DataDetails data;
        private String type;
        private String date;
        private Limits limits;
        private Display display;
        private MemberCreator memberCreator;

        @Data
        public static class DataDetails {
            private Card card;
            private Old old;
            private Board board;
            private ListBefore listBefore;
            private ListAfter listAfter;

            @Data
            public static class Card {
                private String idList;
                private String id;
                private String name;
                private int idShort;
                private String shortLink;
            }

            @Data
            public static class Old {
                private String idList;
            }

            @Data
            public static class Board {
                private String id;
                private String name;
                private String shortLink;
            }

            @Data
            public static class ListBefore {
                private String id;
                private String name;
            }

            @Data
            public static class ListAfter {
                private String id;
                private String name;
            }
        }

        @Data
        public static class Limits {
        }

        @Data
        public static class Display {
            private String translationKey;
            private Entities entities;

            @Data
            public static class Entities {
                private Card card;
                private ListBefore listBefore;
                private ListAfter listAfter;
                private MemberCreator memberCreator;

                @Data
                public static class Card {
                    private String type;
                    private String id;
                    private String idList;
                    private String shortLink;
                    private String text;
                }

                @Data
                public static class ListBefore {
                    private String type;
                    private String id;
                    private String text;
                }

                @Data
                public static class ListAfter {
                    private String type;
                    private String id;
                    private String text;
                }

                @Data
                public static class MemberCreator {
                    private String type;
                    private String id;
                    private String username;
                    private String text;
                }
            }
        }

        @Data
        public static class MemberCreator {
            private String id;
            private boolean activityBlocked;
            private String avatarHash;
            private String avatarUrl;
            private String fullName;
            private String idMemberReferrer;
            private String initials;
            private Map<String, Object> nonPublic;
            private boolean nonPublicAvailable;
            private String username;
        }
    }

    @Data
    public static class Webhook {
        private String id;
        private String description;
        private String idModel;
        private String callbackURL;
        private boolean active;
        private int consecutiveFailures;
        private String firstConsecutiveFailDate;
    }
}
