package pro.trevor.cobblemonuncraftables.gts;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import pro.trevor.cobblemonuncraftables.util.JsonCodec;
import pro.trevor.cobblemonuncraftables.util.ObjectStorage;

public class GtsStorage extends ObjectStorage<Gts> {

    private static final String GTS_IDENTIFIER = "gts";
    private static final JsonCodec<Gts> CODEC = new Gts.GtsCodec();
    private static final Type<GtsStorage> TYPE = new Type<>(GtsStorage::new, GtsStorage::new, null);

    public GtsStorage() {
        super(GTS_IDENTIFIER, new Gts(), CODEC);
    }

    public GtsStorage(NbtCompound nbt, RegistryWrapper.WrapperLookup wrapperLookup) {
        super(nbt, GTS_IDENTIFIER, CODEC);
    }

    public static GtsStorage get(ServerWorld world) {
        GtsStorage storage = world.getPersistentStateManager().getOrCreate(TYPE, GTS_IDENTIFIER);
        storage.markDirty();
        return storage;
    }
}
