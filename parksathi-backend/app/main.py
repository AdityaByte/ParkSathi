from fastapi import FastAPI
from api.v1.router import router as APIRouter

app = FastAPI()

app.include_router(APIRouter, prefix="/api/v1")

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="localhost", port=8080)