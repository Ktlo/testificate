package ktlo.log

/**
 * Ошибка, после которой следует завершить работу программы.
 */
public class FatalError : Error {
    public constructor() : super()
    public constructor(message: String?) : super(message)
    public constructor(cause: Throwable?) : super(cause)
    public constructor(message: String?, cause: Throwable?) : super(message, cause)
}
