package com.auction.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AuctionManager {
    private static volatile AuctionManager instance;
    private final Map<String, AuctionSession> sessions;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private AuctionManager() {
        this.sessions = new ConcurrentHashMap<>();
        startAutoCloseTask();
    }

    public static AuctionManager getInstance() {
        if (instance == null) {
            synchronized (AuctionManager.class) {
                if (instance == null) {
                    instance = new AuctionManager();
                }
            }
        }
        return instance;
    }

    private void startAutoCloseTask() {
        scheduler.scheduleAtFixedRate(() -> {
            LocalDateTime now = LocalDateTime.now();
            for (AuctionSession session : sessions.values()) {
                if ((session.getStatus() == AuctionStatus.RUNNING || session.getStatus() == AuctionStatus.EXTENDED)
                        && session.getEndTime() != null) {

                    try {
                        LocalDateTime endTime = LocalDateTime.parse(session.getEndTime());
                        if (now.isAfter(endTime)) {
                            session.setStatus(AuctionStatus.FINISHED);
                            System.out.println("==> [HỆ THỐNG]: Phiên " + session.getAuctionID() + " đã kết thúc tự động!");
                        }
                    } catch (Exception e) {
                    }
                }
            }
        }, 0, 1, TimeUnit.SECONDS);
    }


    public boolean createSession(String auctionID, String itemID, String sellerID, double startPrice) {
        if (sessions.containsKey(auctionID)) {
            return false;
        }

        AuctionSession newSession = new AuctionSession(auctionID, itemID, sellerID, startPrice);
        sessions.put(auctionID, newSession);
        return true;
    }

    public boolean startSession(String auctionID) {
        AuctionSession session = sessions.get(auctionID);
        if (session == null || session.getStatus() != AuctionStatus.OPEN) {
            return false;
        }

        session.setStatus(AuctionStatus.RUNNING);
        return true;
    }

    public boolean placeBid(String auctionID, String bidderID, double bidAmount) {
        AuctionSession session = sessions.get(auctionID);
        if (session == null) {
            return false;
        }

        return session.processBid(bidderID, bidAmount);
    }

    public boolean closeSession(String auctionID) {
        AuctionSession session = sessions.get(auctionID);
        if (session == null) {
            return false;
        }

        AuctionStatus status = session.getStatus();
        if (status != AuctionStatus.RUNNING && status != AuctionStatus.EXTENDED) {
            return false;
        }

        session.setStatus(AuctionStatus.FINISHED);
        return true;
    }

    public AuctionSession getSession(String auctionID) {
        return sessions.get(auctionID);
    }

    public List<AuctionSession> getAllSessions() {
        return Collections.unmodifiableList(new ArrayList<>(sessions.values()));
    }
}