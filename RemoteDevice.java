
/**
 * A remote proxy implementation for the Device interface.
 */
public class RemoteDevice implements Device
{
    @SuppressWarnings("unchecked")
    @Override
    public <T extends DeviceComponent> T getComponent(Class<T> componentType) throws DeviceComponentException
    {
        boolean initedMetrics = false;
        if (!DeeMetricsRecorder.hasInitedMetrics()) {
            DeeMetricsRecorder.initMetrics(GET_COMPONENT_METRIC_NAME);
            initedMetrics = true;
        }

        try {
            if (componentMap.containsKey(componentType)) {
                DeeMetricsRecorder.recordCount(GET_COMPONENT_METRIC_NAME + "_CacheHit");
                return (T)componentMap.get(componentType);
            } else {
                ArrayList<Object> arguments = new ArrayList<>();
                arguments.add(componentType);

                ArrayList<Invocation.InvocationItem> callChain = new ArrayList<>();
                callChain.add(new Invocation.InvocationItem("getComponent", arguments));

                RemoteObjectInvocationInterceptor deviceComponentCallInterceptor = new RemoteObjectInvocationInterceptor(this, callChain, mInvocationHandler);

                T proxy = (T) Proxy.newProxyInstance(componentType.getClassLoader(), new Class[]{componentType}, deviceComponentCallInterceptor);

                componentMap.put(componentType, proxy);
                DeeMetricsRecorder.recordCount(GET_COMPONENT_METRIC_NAME + "_CacheMiss");

                return proxy;
            }
        } finally {
            if (initedMetrics) {
                DeeMetricsRecorder.close();
            }
        }
    }
}
