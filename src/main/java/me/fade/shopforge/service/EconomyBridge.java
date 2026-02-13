package me.fade.shopforge.service;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.Loader;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Method;
import java.util.UUID;

public class EconomyBridge {

    private static final String ECONOMY_MOD_ID = "privateserver";

    private final Logger logger;

    private boolean initialized;
    private boolean available;
    private Object economyApi;
    private Method getBalanceMethod;
    private Method withdrawMethod;

    public EconomyBridge(Logger logger) {
        this.logger = logger;
    }

    public synchronized WithdrawResult withdraw(EntityPlayerMP player, double amount) {
        if (amount <= 0.0D) {
            return WithdrawResult.success(0.0D);
        }
        if (!ensureApi()) {
            return WithdrawResult.failure("Economy mod is not available.");
        }

        try {
            UUID playerId = player.getUniqueID();
            String playerName = player.getName();
            double currentBalance = ((Number) getBalanceMethod.invoke(economyApi, playerId, playerName)).doubleValue();
            if (currentBalance + 0.00001D < amount) {
                return WithdrawResult.failure("Insufficient balance.");
            }
            double newBalance = ((Number) withdrawMethod.invoke(economyApi, playerId, playerName, amount)).doubleValue();
            return WithdrawResult.success(newBalance);
        } catch (ReflectiveOperationException ex) {
            logger.error("Failed to call economy API", ex);
            initialized = false;
            available = false;
            economyApi = null;
            return WithdrawResult.failure("Economy call failed.");
        }
    }

    public synchronized boolean isAvailable() {
        return ensureApi();
    }

    private boolean ensureApi() {
        if (initialized) {
            return available;
        }

        initialized = true;
        available = false;

        if (!Loader.isModLoaded(ECONOMY_MOD_ID)) {
            return false;
        }

        try {
            Class<?> privateServerClass = Class.forName("me.styna.privateserver.PrivateServer");
            Method getMethod = privateServerClass.getMethod("get");
            Object privateServer = getMethod.invoke(null);
            if (privateServer == null) {
                return false;
            }

            Method getEconomyServiceMethod = privateServerClass.getMethod("getEconomyService");
            Object service = getEconomyServiceMethod.invoke(privateServer);
            if (service == null) {
                return false;
            }

            Class<?> economyApiClass = Class.forName("me.styna.privateserver.api.economy.EconomyApi");
            if (!economyApiClass.isInstance(service)) {
                return false;
            }

            this.economyApi = service;
            this.getBalanceMethod = economyApiClass.getMethod("getBalance", UUID.class, String.class);
            this.withdrawMethod = economyApiClass.getMethod("withdraw", UUID.class, String.class, double.class);
            this.available = true;
        } catch (ReflectiveOperationException ex) {
            logger.error("Could not initialize economy bridge", ex);
            this.available = false;
        }

        return available;
    }

    public static final class WithdrawResult {
        private final boolean success;
        private final double resultingBalance;
        private final String failureReason;

        private WithdrawResult(boolean success, double resultingBalance, String failureReason) {
            this.success = success;
            this.resultingBalance = resultingBalance;
            this.failureReason = failureReason;
        }

        public static WithdrawResult success(double resultingBalance) {
            return new WithdrawResult(true, resultingBalance, null);
        }

        public static WithdrawResult failure(String failureReason) {
            return new WithdrawResult(false, 0.0D, failureReason);
        }

        public boolean isSuccess() {
            return success;
        }

        public double getResultingBalance() {
            return resultingBalance;
        }

        public String getFailureReason() {
            return failureReason;
        }
    }
}