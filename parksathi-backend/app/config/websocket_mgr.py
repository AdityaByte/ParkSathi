from fastapi import WebSocket

class WebSocketManager:
    def __init__(self):
        self.active_connections: dict[str, list[WebSocket]] = {}
        
    async def connect(self, websocket: WebSocket, uid: str):
        if uid not in self.active_connections:
            self.active_connections[uid] = []
        self.active_connections[uid].append(websocket)
    
    def disconnect(self, websocket: WebSocket, uid: str):
        if uid in self.active_connections:
            if websocket in self.active_connections[uid]:
                self.active_connections[uid].remove(websocket)
            if not self.active_connections[uid]:
                del self.active_connections[uid]

    async def broadcast_to_partner(self, uid: str, message: dict):
        if uid in self.active_connections:
            for connection in self.active_connections[uid]:
                await connection.send_json(message)
    
manager = WebSocketManager()