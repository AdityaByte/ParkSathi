"""Service layer for the parking routes."""

from fastapi import HTTPException

from app.model.parking import ParkingDetails, VerificationStatus
from app.schema.parking_spot_response import Coordinates, NearbyParkingSpot

async def find_nearby_parking_spot(lat: float, lng: float) -> list[NearbyParkingSpot]:
    METERS_IN_KM = 1000

    pipeline = [
        {
            "$geoNear": {
                "near": {"type": "Point", "coordinates": [lng, lat]},
                "distanceField": "calculated_distance",
                "maxDistance": METERS_IN_KM,
                "query": {"verification_status": VerificationStatus.APPROVED},
                "spherical": True
            }
        },
        {"$limit": 1} 
    ]
    
    collection = ParkingDetails.get_pymongo_collection()
    cursor = collection.aggregate(pipeline)
    results = await cursor.to_list(length=10) # We are expecting 10 spots right now.

    if not results:
        raise HTTPException(status_code=404, detail="No nearby parking spot found")

    spot = results[0]

    return [
        NearbyParkingSpot(
            uid=spot["uid"],
            parking_name=str(spot["parking_name"]),
            parking_id=str(spot['parking_id']),
            address=spot["address"],
            phone_number=spot["phone_number"],
            coordinates=Coordinates(
                lat=spot["coordinates"]["coordinates"][1], 
                lng=spot["coordinates"]["coordinates"][0]
            ),
            slots=spot["slots"],
            available_slots=spot["slots"],
            verification_status=spot["verification_status"],
            distance=round(spot["calculated_distance"] / 1000, 2)
        ) for spot in results
    ] 