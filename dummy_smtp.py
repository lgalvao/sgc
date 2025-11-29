import socket
import threading
import sys

def handle_client(conn, addr):
    print(f"Connected by {addr}")
    try:
        conn.sendall(b"220 Dummy SMTP Server\r\n")
        while True:
            data = conn.recv(1024)
            if not data: break
            print(f"Received: {data}")
            if data.upper().startswith(b"QUIT"):
                conn.sendall(b"221 Bye\r\n")
                break
            if data.upper().startswith(b"DATA"):
                conn.sendall(b"354 End data with <CR><LF>.<CR><LF>\r\n")
                continue
            # Respond to everything else with 250 OK
            conn.sendall(b"250 OK\r\n")
    except Exception as e:
        print(f"Error: {e}")
    finally:
        conn.close()

def run():
    host = 'localhost'
    port = 1025
    
    # Try to bind to IPv6 first, then IPv4
    addr_info = socket.getaddrinfo(host, port, socket.AF_UNSPEC, socket.SOCK_STREAM)
    
    for res in addr_info:
        af, socktype, proto, canonname, sa = res
        try:
            s = socket.socket(af, socktype, proto)
            s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
            # If IPv6, try to enable dual stack if possible (IPV6_V6ONLY=0)
            if af == socket.AF_INET6:
                try:
                    s.setsockopt(socket.IPPROTO_IPV6, socket.IPV6_V6ONLY, 0)
                except:
                    pass
            s.bind(sa)
            s.listen()
            print(f"Listening on {sa}")
            while True:
                conn, addr = s.accept()
                threading.Thread(target=handle_client, args=(conn, addr)).start()
            break # If successful, break
        except OSError as msg:
            s.close()
            continue
    else:
        print("Could not open socket")
        sys.exit(1)

if __name__ == '__main__':
    run()
