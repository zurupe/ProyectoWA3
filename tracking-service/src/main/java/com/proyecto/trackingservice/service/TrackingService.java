package com.proyecto.trackingservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class TrackingService {
    private final StringRedisTemplate redisTemplate;

    @Autowired
    public TrackingService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String getEstadoPedido(String pedidoId) {
        return redisTemplate.opsForValue().get(pedidoId);
    }

    public void actualizarEstadoPedido(String pedidoId, String estado) {
        redisTemplate.opsForValue().set(pedidoId, estado);
    }
}
