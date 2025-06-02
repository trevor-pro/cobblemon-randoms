package pro.trevor.cobblemonuncraftables.util;

import com.google.gson.JsonParser;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.PersistentState;

import java.io.File;

public abstract class ObjectStorage<T> extends PersistentState {

    protected static final int CHUNK_SIZE = 65535;

    protected static final String OBJECT_CHUNKS_KEY_FORMAT = Nbt.PREFIX + "%s_chunks";
    protected static final String OBJECT_CHUNK_REMAINDER_KEY_FORMAT = Nbt.PREFIX + "%s_chunks_remainder";
    protected static final String OBJECT_CHUNK_KEY_PREFIX_FORMAT = Nbt.PREFIX + "%s_chunk_";


    protected final String objectChunksKey;
    protected final String objectChunkRemainderKey;
    protected final String objectChunkKeyPrefix;

    protected final JsonCodec<T> codec;

    protected final T object;

    public ObjectStorage(String identifier, T object, JsonCodec<T> codec) {
        this.objectChunksKey = OBJECT_CHUNKS_KEY_FORMAT.formatted(identifier);
        this.objectChunkRemainderKey = OBJECT_CHUNK_REMAINDER_KEY_FORMAT.formatted(identifier);
        this.objectChunkKeyPrefix = OBJECT_CHUNK_KEY_PREFIX_FORMAT.formatted(identifier);
        this.object = object;
        this.codec = codec;
    }

    public ObjectStorage(NbtCompound nbt, String identifier, JsonCodec<T> codec) {
        this.objectChunksKey = OBJECT_CHUNKS_KEY_FORMAT.formatted(identifier);
        this.objectChunkRemainderKey = OBJECT_CHUNK_REMAINDER_KEY_FORMAT.formatted(identifier);
        this.objectChunkKeyPrefix = OBJECT_CHUNK_KEY_PREFIX_FORMAT.formatted(identifier);
        this.codec = codec;

        int chunks = nbt.getInt(objectChunksKey);
        int chunkRemainder = nbt.getInt(objectChunkRemainderKey);

        byte[] storage = new byte[(chunks * CHUNK_SIZE) + chunkRemainder];

        // To work around the 16-bit byte array restriction, we store/read in chunks of arrays of CHUNK_SIZE bytes
        for (int i = 0; i < chunks; ++i) {
            System.arraycopy(nbt.getByteArray(objectChunkKeyPrefix + i), 0, storage, i * CHUNK_SIZE, CHUNK_SIZE);
        }
        System.arraycopy(nbt.getByteArray(objectChunkKeyPrefix + chunks), 0, storage, chunks * CHUNK_SIZE, chunkRemainder);

        String objectNbtData = new String(storage);

        this.object = codec.decode(JsonParser.parseString(objectNbtData).getAsJsonObject());
    }

    @Override
    public final NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        synchronized (this) {
            byte[] bytes = codec.encode(object).toString().getBytes();
            int chunks = bytes.length / CHUNK_SIZE;
            int chunkRemainder = bytes.length % CHUNK_SIZE;

            nbt.putInt(objectChunksKey, chunks);
            nbt.putInt(objectChunkRemainderKey, chunkRemainder);
            for (int i = 0; i < chunks; ++i) {
                byte[] chunkBytes = new byte[CHUNK_SIZE];
                System.arraycopy(bytes, i * CHUNK_SIZE, chunkBytes, 0, CHUNK_SIZE);
                nbt.putByteArray(objectChunkKeyPrefix + i, chunkBytes);
            }
            byte[] remainderBytes = new byte[chunkRemainder];
            System.arraycopy(bytes, chunks * CHUNK_SIZE, remainderBytes, 0, chunkRemainder);
            nbt.putByteArray(objectChunkKeyPrefix + chunks, remainderBytes);
        }

        return nbt;
    }

    public final T data() {
        synchronized (this) {
            return object;
        }
    }

    public final void flagForSaving() {
        synchronized (this) {
            markDirty();
        }
    }

    @Override
    public final void save(File file, RegistryWrapper.WrapperLookup registryLookup) {
        synchronized (this) {
            super.save(file, registryLookup);
        }
    }
}
