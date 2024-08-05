package com.itsaur.elasticsearch.index.refresh.plugin;

import java.util.Objects;

/**
 * Represents a callback action that will be executed after an index was refreshed.
 */
public interface IndexRefreshedCallback {

    Settings.Builder DEFAULT_SETTINGS = Settings.builder();

    /**
     * The callback method to implement. It will be called every time the associated index is refreshed.
     * @param refreshCount The number that the index was refreshed so far.
     */
    void refreshed(long refreshCount);

    record Settings(boolean ephemeral) {
        public static Builder builder() {
            return new Builder();
        }

        public static final class Builder {

            /**
             * Defines if this callback should be deleted automatically after the 1st execution.
             * That means that the call callback will be executed only once (when the associated index was refreshed)
             * and then will be deleted.
             * If a callback is not ephemeral, it will be getting fired on each index refresh unless it is removed manually.
             * See {@link IndexesRefreshManager#removeCallback(String, String)}
             */
            private boolean ephemeral;

            public Settings build() {
                return new Settings(ephemeral);
            }

            public Builder withEphemeral(boolean ephemeral) {
                this.ephemeral = ephemeral;
                return this;
            }
        }
    }

    record CallbackWithSettings(IndexRefreshedCallback callback, Settings settings) {
        public CallbackWithSettings {
            Objects.requireNonNull(callback);
            Objects.requireNonNull(settings);
        }
    }
}
