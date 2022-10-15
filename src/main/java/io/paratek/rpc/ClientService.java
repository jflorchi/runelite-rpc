package io.paratek.rpc;

import io.grpc.stub.StreamObserver;
import io.paratek.grpc.Client;
import io.paratek.grpc.ClientServiceGrpc;

import java.awt.*;

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
    public void setLoginInfo(Client.LoginRequest request, StreamObserver<Client.BooleanResponse> responseObserver) {
        this.rlClient.setUsername(request.getUsername());
        this.rlClient.setPassword(request.getPassword());

        Client.BooleanResponse response = Client.BooleanResponse.newBuilder()
                .setStatus(true)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
