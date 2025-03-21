package com.levi.gateway.factory;


import net.openhft.affinity.AffinityStrategies;
import net.openhft.affinity.AffinityThreadFactory;

public class CustomAffinityThreadFactory extends AffinityThreadFactory {
    public CustomAffinityThreadFactory(String name, AffinityStrategies... strategies) {
        super(name, strategies);
    }
}