package ru.vladofficial.MyDI.Scanning;

import java.util.List;

public record ScanningResult(List<Class<?>> components, List<Class<?>> configs) {}