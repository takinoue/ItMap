package cc.atte.itmap

data class JsonApiModel(
    var request: Request?,
    var response: Response?
) {
    data class Request(
        var keyword: String?,
        var message: String?,
        var coordinate: List<Coordinate>?
    ) {
        data class Coordinate(
            var timestamp: Double,
            var longitude: Double,
            var latitude: Double,
            var altitude: Double? = null
        )
    }
    data class Response(
        var success: Success?,
        var failure: Failure?
    ) {
        data class Success(
            var count: Int
        )
        data class Failure(
            var message: String
        )
    }
}