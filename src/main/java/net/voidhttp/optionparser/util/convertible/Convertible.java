package net.voidhttp.optionparser.util.convertible;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Represents a value converter, that tries to convert the given raw data to a T object.
 * <br><br>
 * This class also contains useful methods to add transformers and completion handlers.
 * <br>
 * Error recovery is also possible using {@link #fallback(Object)}, {@link #fallback(Supplier)} and {@link #fallback(Function)}.
 * <br>
 * The syntax encourages chaining, therefore less code is needed to handle certain tasks/events.
 * @param <T> the type of the object that will be converted
 * @param <U> the type of the returned value of the completed conversion
 * @author AdvancedAntiSkid
 * @since 1.0
 */
public abstract class Convertible<T, U> {
    /**
     * The raw input data of the convertible that will be converted.
     */
    protected final T data;

    /**
     * The value of the completion result. Initially <code>null</code>, it is set to the completion object
     * after the completion is finished (which might still be <code>null</code>).
     */
    private U result;

    /**
     * The error that occurred whilst executing and caused an convertible failure.
     * Initially <code>null</code>, after a failure, it is guaranteed to be non-null.
     */
    private Throwable error;

    /**
     * Indicates whether the completion had been done (either successfully or unsuccessfully).
     */
    private boolean completed;

    /**
     * Indicates whether the completion was failed.
     */
    private boolean failed;

    /**
     * Create a new, uncompleted convertible.
     * @param data raw convertible input to be converted
     */
    public Convertible(T data) {
        this.data = data;
    }

    /**
     * Try to convert the input value to the given T type.
     * @return converted value
     * @throws Exception unable to convert
     */
    protected abstract U convert() throws Exception;

    /**
     * Try to convert the raw input data to the given T type.
     * <br><br>
     * If the an error occurs whilst completing, a {@link ConversionException} is thrown.
     * The actual exception that made the completion fail can be obtained using {@link ConversionException#getCause()}.
     * <br><br>
     *
     * @return the completion value or a default value
     * @throws ConversionException the completion failed and a default value was not specified
     */
    public U get() throws ConversionException {
        return tryGetValue(false, null);
    }

    /**
     * Try to convert the raw input data to the given T type.
     * <br><br>
     * If the an error occurs whilst completing, a the <code>defaultValue</code> is returned if present.
     * <br><br>
     * @return the completion value or a default value
     */
    public U getOrDefault(U defaultValue) {
        try {
            return tryGetValue(true, defaultValue);
        } catch (ConversionException e) {
            throw new IllegalStateException("This should have been avoided", e);
        }
    }

    /**
     * Try to convert the raw input data to the given T type.
     * <br><br>
     * If the an error occurs whilst completing, a {@link ConversionException} is thrown,
     * or the <code>defaultValue</code> is returned if present.
     * The actual exception that made the completion fail can be obtained using {@link ConversionException#getCause()}.
     * <br><br>
     *
     * @param hasDefault indicates whether a default value should be returned on a completion failure
     * @param defaultValue the default value which is returned on a completion failure
     * @return the completion value or a default value
     *
     * @throws ConversionException the completion failed and a default value was not specified
     *
     * @see #get()
     * @see #getOrDefault(Object)
     */
    private U tryGetValue(boolean hasDefault, U defaultValue) throws ConversionException {
        // check if the conversion has been already completed
        if (completed) {
            // check if the conversion was successful
            if (!failed)
                return result;
            // completion was unsuccessful
            // return a default value if it is given
            if (hasDefault)
                return defaultValue;
            // no default value set, throw an error
            throw new ConversionException(error);
        }
        // the conversion is not done yet
        try {
            // try to convert the raw string to the required value
            return result = convert();
        } catch (Exception e) {
            // the conversion was unsuccessful
            failed = true;
            // return a default value if it is given
            if (hasDefault)
                return defaultValue;
            // no default value set, throw an error
            throw new ConversionException(error);
        }
    }

    /**
     * Create a new convertible that use the fallback value as a result if the completion fails.
     * <br><br>
     * If this convertible completes successfully, the new convertible will be completed with the same exact value.
     * <br><br>
     * If this convertible fails with an exception, the fallback value will be used to complete the new convertible.
     * This can be used for error recovery, or to produce a fallback object,
     * that will be returned upon unsuccessful completion.
     * <br><br>
     * If the fallback object is not a constant, consider using {@link #fallback(Supplier)} instead,
     * to allow dynamic fallback object creation.
     * If you want to create a fallback value based on the error, use {@link #fallback(Function)} instead.
     * <br><br>
     *
     * @param value the value used for completion if an exception occurs
     * @return a new convertible
     */
    public Convertible<T, U> fallback(U value) {
        // check if the conversion is already completed
        if (completed) {
            // create a completed convertible with the default value
            // if the completion was failed
            if (failed)
                return completed(value);
            // check if the convertible is empty
            if (isEmpty())
                return completed(value);
            // convertible was completed, no need for the fallback value
            return completed(result);
        }
        // create a new convertible that will return the fallback value
        // if the conversion fails
        Convertible<T, U> handle = this;
        return new Convertible<T, U>(data) {
            // add a conversion hook to this convertible
            @Override
            protected U convert() {
                // try to convert the value
                try {
                    return handle.convert();
                }
                // unable to convert, return the fallback value
                catch (Exception e) {
                    return value;
                }
            }
        };
    }

    /**
     * Create a new convertible that use the fallback value as a result if the completion fails.
     * <br><br>
     * If this convertible completes successfully, the new convertible will be completed with the same exact value.
     * <br><br>
     * If this convertible fails with an exception, the fallback value will be used to complete the new convertible.
     * This can be used for error recovery, or to produce a fallback object,
     * that will be returned upon unsuccessful completion.
     * <br><br>
     * If the fallback object is a constant, consider using {@link #fallback(Object)} instead,
     * as it does not require allocating a Supplier.
     * If you want to create a fallback value based on the error, use {@link #fallback(Function)} instead.
     * <br><br>
     *
     * @param supplier the value used for completion if an exception occurs
     * @return a new convertible
     */
    public Convertible<T, U> fallback(Supplier<U> supplier) {
        // check if the conversion is already completed
        if (completed) {
            // create a completed convertible with the default value
            // if the completion was failed
            if (failed)
                return completed(supplier.get());
            // convertible was completed, no need for the fallback value
            return this;
        }
        // create a new convertible that will return the fallback value
        // if the conversion fails
        Convertible<T, U> handle = this;
        return new Convertible<T, U>(data) {
            // add a conversion hook to this convertible
            @Override
            protected U convert() {
                // try to convert the value
                try {
                    return handle.convert();
                }
                // unable to convert, return the fallback value
                catch (Exception e) {
                    return supplier.get();
                }
            }
        };
    }

    /**
     * Create a new convertible that use the fallback value as a result if the completion fails.
     * <br><br>
     * If this convertible completes successfully, the new convertible will be completed with the same exact value.
     * <br><br>
     * If this convertible fails with an exception, the fallback value will be used to complete the new convertible.
     * This can be used for error recovery, or to produce a fallback object,
     * that will be returned upon unsuccessful completion.
     * <br><br>
     * If the fallback object is not a constant, consider using {@link #fallback(Supplier)} instead,
     * to allow dynamic fallback object creation.
     * If the fallback object is a constant, consider using {@link #fallback(Object)} instead,
     * as it does not require allocating a Supplier.
     * <br><br>
     *
     * @param transformer the value used for completion if an exception occurs
     * @return a new convertible
     */
    public Convertible<T, U> fallback(Function<Throwable, U> transformer) {
        // check if the conversion is already completed
        if (completed) {
            // create a completed convertible with the default value
            // if the completion was failed
            if (failed)
                return completed(transformer.apply(error));
            // convertible was completed, no need for the fallback value
            return this;
        }
        // create a new convertible that will return the fallback value
        // if the conversion fails
        Convertible<T, U> handle = this;
        return new Convertible<T, U>(data) {
            // add a conversion hook to this convertible
            @Override
            protected U convert() {
                // try to convert the value
                try {
                    return handle.convert();
                }
                // unable to convert, return the fallback value
                catch (Exception e) {
                    return transformer.apply(e);
                }
            }
        };
    }

    /**
     * Indicates whether the convertible conversion had been done (either successfully or unsuccessfully).
     * <br><br>
     * In order to determine if the conversion was successful, use {@link #isFailed()}.
     * <br><br>
     * @return <code>true</code> if this convertible has been already completed, <code>false</code> otherwise
     * @see #isFailed()
     */
    public boolean isCompleted() {
        return completed;
    }

    /**
     * Indicates whether the convertible conversion was completed unsuccessfully.
     * <br><br>
     * If the convertible hasn't been completed yet, this method returns <code>false</code>.
     * <br><br>
     * @return <code>true</code> if the conversion was unsuccessful, <code>false</code> otherwise
     * @see #isCompleted()
     */
    public boolean isFailed() {
        return failed;
    }

    /**
     * Indicates whether the convertible conversion was completed unsuccessfully or the result value is null.
     * <br><br>
     * If the convertible hasn't been completed yet, this method returns <code>false</code>.
     * @return <code>true</code> if the conversion was unsuccessful or the result is null, <code>false</code> otherwise
     * @see #isFailed()
     */
    public boolean isFailedOrNull() {
        return failed || (completed && result == null);
    }

    /**
     * Indicates whether the convertible is empty. (Has a result value of null.)
     * @return <code>true</code> if the <code>data</code> is null, <code>false</code> otherwise.
     */
    public boolean isEmpty() {
        return completed && result == null;
    }

    /**
     * Create a new convertible, that is initially is completed initially using the specified value.
     * @param value the conversion result
     * @param <T> the source type
     * @param <U> the result type
     * @return a new, completed convertible
     */
    public static <T, U> Convertible<T, U> completed(U value) {
        // create a new convertible
        Convertible<T, U> convertible = new Convertible<T, U>(null) {
            @Override
            protected U convert() {
                return value;
            }
        };
        // set the convertible state
        convertible.result = value;
        convertible.completed = true;
        return convertible;
    }

    /**
     * Create a new convertible, that is initially failed using the specified error.
     * @param error the conversion error
     * @param <T> the source type
     * @param <U> the result type
     * @return a new, failed convertible
     */
    public static <T, U> Convertible<T, U> failed(Throwable error) {
        // create a new convertible
        Convertible<T, U> convertible = new Convertible<T, U>(null) {
            @Override
            protected U convert() {
                return null;
            }
        };
        // set the convertible state
        convertible.error = error;
        convertible.completed = true;
        convertible.failed = true;
        return convertible;
    }

    /**
     * Create a new convertible of the <code>T</code> type, and convert it to <code>U</code>.
     * @param data the value to be converted
     * @param transformer the value converter
     * @param <T> the source type
     * @param <U> the result type
     * @return a new convertible
     */
    public static <T, U> Convertible<T, U> of(T data, Function<T, U> transformer) {
        return new Convertible<T, U>(data) {
            @Override
            protected U convert() {
                return transformer.apply(data);
            }
        };
    }

    /**
     * Create a new, empty convertible.
     * @param <T> the source type
     * @param <U> the result type
     * @return a new, empty convertible
     */
    public static <T, U> Convertible<T, U> empty() {
        Convertible<T, U> convertible = new Convertible<T, U>(null) {
            @Override
            protected U convert() {
                return null;
            }
        };
        convertible.completed = true;
        return convertible;
    }
}
