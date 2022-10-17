package io.paratek.rpc;

import io.grpc.stub.StreamObserver;
import io.paratek.grpc.Client;
import io.paratek.grpc.ClientServiceGrpc;
import net.runelite.api.Player;
import net.runelite.api.PlayerComposition;
import net.runelite.api.geometry.SimplePolygon;
import net.runelite.client.callback.ClientThread;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ClientService extends ClientServiceGrpc.ClientServiceImplBase {

    private final net.runelite.api.Client rlClient;

    public ClientService(net.runelite.api.Client rlClient) {
        this.rlClient = rlClient;
    }

    @Override
    public void getCamera(Client.EmptyRequest request, StreamObserver<Client.Camera> responseObserver) {
        Client.Camera response = Client.Camera.newBuilder()
                .setYaw(this.rlClient.getCameraYaw())
                .setPitch(this.rlClient.getCameraPitch())
                .setX(this.rlClient.getCameraX())
                .setY(this.rlClient.getCameraY())
                .setZ(this.rlClient.getCameraZ())
                .setZoom(this.rlClient.get3dZoom())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getScreen(Client.EmptyRequest request, StreamObserver<Client.Screen> responseObserver) {
        final Point location = this.rlClient.getCanvas().getLocationOnScreen();
        final Rectangle bounds = this.rlClient.getCanvas().getBounds();
        Client.Screen response = Client.Screen.newBuilder()
                .setX(location.x)
                .setY(location.y)
                .setWidth(bounds.width)
                .setHeight(bounds.height)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getGameState(Client.EmptyRequest request, StreamObserver<Client.GameState> responseObserver) {
        Client.GameState response = Client.GameState.newBuilder()
                .setState(this.rlClient.getGameState().getState())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getLoginState(Client.EmptyRequest request, StreamObserver<Client.LoginState> responseObserver) {
        Client.LoginState response = Client.LoginState.newBuilder()
                .setState(this.rlClient.getLoginIndex())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void setLoginInfo(Client.LoginInfo request, StreamObserver<Client.BooleanResponse> responseObserver) {
        this.rlClient.setUsername(request.getUsername());
        this.rlClient.setPassword(request.getPassword());

        Client.BooleanResponse response = Client.BooleanResponse.newBuilder()
                .setStatus(true)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getViewPort(Client.EmptyRequest request, StreamObserver<Client.Rectangle> responseObserver) {
        Client.Rectangle response = Client.Rectangle.newBuilder()
                .setX(this.rlClient.getViewportXOffset())
                .setY(this.rlClient.getViewportYOffset())
                .setWidth(this.rlClient.getViewportWidth())
                .setHeight(this.rlClient.getViewportHeight())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getMouseInfo(Client.EmptyRequest request, StreamObserver<Client.MouseInfo> responseObserver) {
        final net.runelite.api.Point mouseLocation = this.rlClient.getMouseCanvasPosition();
        Client.MouseInfo response = Client.MouseInfo.newBuilder()
                .setCanvasX(mouseLocation.getX())
                .setCanvasY(mouseLocation.getY())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getLocalPlayer(Client.EmptyRequest request, StreamObserver<Client.Player> responseObserver) {
        final Player local = this.rlClient.getLocalPlayer();
        if (local == null) {
            responseObserver.onNext(null);
            responseObserver.onCompleted();
            return;
        }
        responseObserver.onNext(this.packPlayer(local));
        responseObserver.onCompleted();
    }

    @Override
    public void getLoadedPlayers(Client.EmptyRequest request, StreamObserver<Client.LoadedPlayers> responseObserver) {
        final List<Player> players = this.rlClient.getPlayers();
        Client.LoadedPlayers.Builder loadedPlayersBuilder = Client.LoadedPlayers.newBuilder();
        for (Player p : players) {
            if (p == null) {
                continue;
            }
            final Client.Player packedPlayer = this.packPlayer(p);
            if (packedPlayer != null) {
                loadedPlayersBuilder.addPlayers(packedPlayer);
            }
        }
        responseObserver.onNext(loadedPlayersBuilder.build());
        responseObserver.onCompleted();
    }

    private Client.Player packPlayer(final Player player) {
        if (player == null || player.getName() == null) {
            return null;
        }

        Client.PlayerComposition.Builder compositionResponse = Client.PlayerComposition.newBuilder();
        final PlayerComposition composition = player.getPlayerComposition();
        if (composition != null) {
            for (int color : composition.getColors()) {
                compositionResponse.addColors(color);
            }
            for (int equip : composition.getEquipmentIds()) {
                compositionResponse.addEquipmentIds(equip);
            }
        }

        final Rectangle bounds = player.getConvexHull().getBounds();
        final Client.Rectangle boundsResp = Client.Rectangle.newBuilder()
                .setX(bounds.x)
                .setY(bounds.y)
                .setWidth(bounds.width)
                .setHeight(bounds.height)
                .build();

        final Client.Tile worldTile = Client.Tile.newBuilder()
                .setX(player.getWorldLocation().getX())
                .setY(player.getWorldLocation().getY())
                .setPlane(player.getWorldLocation().getPlane())
                .build();
        final Client.Tile regionTile = Client.Tile.newBuilder()
                .setX(player.getLocalLocation().getX())
                .setY(player.getLocalLocation().getY())
                .setPlane(player.getWorldLocation().getPlane())
                .build();

        Client.Player.Builder response = Client.Player.newBuilder()
                .setId(player.getId())
                .setName(player.getName())
                .setAnimation(player.getAnimation())
                .setPoseAnimation(player.getPoseAnimation())
                .setCombatLevel(player.getCombatLevel())
                .setTeam(player.getTeam())
                .setFriendsChatMember(player.isFriendsChatMember())
                .setFriend(player.isFriend())
                .setClanMember(player.isClanMember())
                .setHeadIcon(player.getOverheadIcon() != null ? player.getOverheadIcon().ordinal() : -1)
                .setSkullIcon(player.getSkullIcon() != null ? player.getSkullIcon().ordinal() : -1)
                .setComposition(compositionResponse.build())
                .setOverheadText(player.getOverheadText() == null ? "" : player.getOverheadText())
                .setDead(player.isDead())
                .setInteracting(player.getInteracting() != null)
                .setOrientation(player.getOrientation())
                .setBounds(boundsResp)
                .setWorldLocation(worldTile)
                .setRegionLocation(regionTile);

        return response.build();
    }

}
