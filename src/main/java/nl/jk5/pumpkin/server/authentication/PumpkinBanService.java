package nl.jk5.pumpkin.server.authentication;

import nl.jk5.pumpkin.server.Pumpkin;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.service.ban.BanService;
import org.spongepowered.api.util.ban.Ban;

import java.net.InetAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

public class PumpkinBanService implements BanService {

    private final Pumpkin pumpkin;

    public PumpkinBanService(Pumpkin pumpkin) {
        this.pumpkin = pumpkin;
    }

    @Override
    public Collection<? extends Ban> getBans() {
        return Collections.emptyList();
    }

    @Override
    public Collection<Ban.Profile> getProfileBans() {
        return Collections.emptyList();
    }

    @Override
    public Collection<Ban.Ip> getIpBans() {
        return Collections.emptyList();
    }

    @Override
    public Optional<Ban.Profile> getBanFor(GameProfile profile) {
        return Optional.empty();
    }

    @Override
    public Optional<Ban.Ip> getBanFor(InetAddress address) {
        return Optional.empty();
    }

    @Override
    public boolean isBanned(GameProfile profile) {
        return false;
    }

    @Override
    public boolean isBanned(InetAddress address) {
        return false;
    }

    @Override
    public boolean pardon(GameProfile profile) {
        return false;
    }

    @Override
    public boolean pardon(InetAddress address) {
        return false;
    }

    @Override
    public boolean removeBan(Ban ban) {
        return false;
    }

    @Override
    public Optional<? extends Ban> addBan(Ban ban) {
        return Optional.empty();
    }

    @Override
    public boolean hasBan(Ban ban) {
        return false;
    }
}
