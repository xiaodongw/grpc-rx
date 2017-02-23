package io.grpc.rx.core;

import io.grpc.ClientCall;
import io.grpc.Metadata;
import io.grpc.Status;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link io.grpc.ClientCall.Listener} which forwards all of its methods to several {@link io.grpc.ClientCall.Listener}.
 */
public class DelegateClientCallListener<T> extends ClientCall.Listener<T> {
  private ClientCall.Listener[] listeners;

  public DelegateClientCallListener(ClientCall.Listener... listeners) {
    this.listeners = listeners;
  }

  @Override
  public void onHeaders(Metadata headers) {
    for (ClientCall.Listener<T> listener : listeners) {
      listener.onHeaders(headers);
    }
  }

  @Override
  public void onMessage(T message) {
    for (ClientCall.Listener<T> listener : listeners) {
      listener.onMessage(message);
    }
  }

  @Override
  public void onClose(Status status, Metadata trailers) {
    for (ClientCall.Listener<T> listener : listeners) {
      listener.onClose(status, trailers);
    }
  }

  @Override
  public void onReady() {
    for (ClientCall.Listener<T> listener : listeners) {
      listener.onReady();
    }
  }
}
