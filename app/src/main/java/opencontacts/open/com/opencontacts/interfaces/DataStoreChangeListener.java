package opencontacts.open.com.opencontacts.interfaces;

public interface DataStoreChangeListener<T> {
        void onUpdate(T t);
        void onRemove(T t);
        void onAdd(T t);
        void onStoreRefreshed();
}
