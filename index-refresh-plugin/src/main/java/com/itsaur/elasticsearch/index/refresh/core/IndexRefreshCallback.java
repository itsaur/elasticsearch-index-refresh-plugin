package com.itsaur.elasticsearch.index.refresh.core;

/**
 * Represents a callback action that will be executed after an index was refreshed.
 */
public interface IndexRefreshCallback {

    Settings.Builder DEFAULT_SETTINGS = Settings.builder();

    /**
     * The callback method to implement. It will be called every time the associated index is refreshed.
     * @param refreshCount The number that the index was refreshed so far.
     */
    void refreshed(long refreshCount);

    /**
     * Settings that define how the call back should be handled.
     *
     * @param ephemeral Defines if this callback should be deleted automatically after the 1st execution.
     *                  That means that the callback will be executed only once (when the associated index was refreshed)
     *                  and then will be deleted.
     *                  <p>If a callback is not ephemeral, it will be getting fired on each index refresh unless it is removed manually.</p>
     *                  @see IndexesRefreshManager#removeCallback(String, String)
     */
    record Settings(boolean ephemeral) {
        public static Builder builder() {
            return new Builder();
        }

        public static final class Builder {

            private boolean ephemeral;

            public Settings build() {
                return new Settings(ephemeral);
            }

            /**
             * Defines if this callback should be deleted automatically after the 1st execution.
             * That means that the callback will be executed only once (when the associated index was refreshed)
             * and then will be deleted.
             * <p>If a callback is not ephemeral, it will be getting fired on each index refresh unless it is removed manually.</p>
             *
             * @param ephemeral true/false to define if the callback is ephemeral or not
             * @see IndexesRefreshManager#removeCallback(String, String)
             */
            public Builder withEphemeral(boolean ephemeral) {
                this.ephemeral = ephemeral;
                return this;
            }
        }
    }

}
