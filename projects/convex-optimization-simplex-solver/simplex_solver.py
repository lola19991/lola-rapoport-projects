import numpy as np


def projectiveGradientDescend(H: np.ndarray, y: np.ndarray, A: np.ndarray, B: np.ndarray) -> np.ndarray:
    x = projection(np.matmul(psuedoInv(H), y))
    epsilon = 10**(-8)
    for i in range(10**3):
        x = projection(x - epsilon * (np.matmul(A, x) + B))
    return x


def interiorPointNewtonLogBarrier(length, B: np.ndarray, C: np.ndarray) -> np.ndarray:
    A = np.ones((1, length))
    Lambda = np.random.uniform(0, 1)
    tol = 10**4
    x = 1/length*np.ones((length, 1))
    close = np.linalg.norm(np.concatenate(
        (gradl(A, B, C, x, Lambda, tol), np.matmul(A, x) + 1), axis=0))
    while close > 2.01:
        temp1 = np.concatenate((hessianf(B, x, tol), np.transpose(A)), axis=1)
        temp2 = np.concatenate((A, np.matrix('0')), axis=1)
        temp3 = np.concatenate([temp1, temp2])
        temp4 = np.concatenate([-gradl(A, B, C, x, Lambda, tol), -np.matmul(A, x) + 1])
        v = np.linalg.solve(temp3, temp4)
        if is_unfeasible(x + v[:length]):
            return x
        x += v[:length]
        Lambda += v[-1]
        close = np.linalg.norm(np.concatenate(
            (gradl(A, B, C, x, Lambda, tol), np.matmul(A, x) + 1), axis=0))
    return x


def is_unfeasible(x):
    return np.min(x) <= 0


def solve(H: np.ndarray, y: np.ndarray) -> np.ndarray:
    y = np.transpose(np.array([y]))
    A = np.matmul(np.transpose(H), H)
    B = -np.matmul(np.transpose(H), y)
    length = H.shape[1]
    if condest(A) > 2200000000:
        return np.ndarray((length, ), buffer=interiorPointNewtonLogBarrier(length, A, B))
    else:
        return np.ndarray((length, ), buffer=projectiveGradientDescend(H, y, A, B))


def condest(A: np.ndarray) -> np.ndarray:
    A1 = np.linalg.solve(A, np.identity(A.shape[0]))
    return (np.linalg.norm(A)) * (np.linalg.norm(A1))


def gradf(A: np.ndarray, B: np.ndarray, x: np.ndarray, tol) -> np.ndarray:
    return np.matmul(A, x) + B - 1/tol * 1./x


def hessianf(A: np.ndarray, x: np.ndarray, tol) -> np.ndarray:
    return A + 1/tol * np.diag(1./(np.power(x, 2)))


def gradl(A: np.ndarray, B: np.ndarray, C: np.ndarray, x: np.ndarray, Lambda, tol) -> np.ndarray:
    return gradf(B, C, x, tol) + np.matmul(A, x) * Lambda


def psuedoInv(M):
    U, S, VT = np.linalg.svd(M)
    Z = np.zeros(M.shape)
    iter = min(Z.shape)
    Z[:iter, :iter] = np.diag(S)
    for i in range(iter):
        if Z[i, i] != 0:
            Z[i, i] = 1 / Z[i, i]
    return np.transpose(np.matmul(np.matmul(U, Z), VT))


def projection(p: np.ndarray) -> np.ndarray:
    u = sorted(p)
    u = np.flip(u)
    length = u.size
    s = 0
    m = 1
    for i in range(length):
        s += u[i]
        if u[i] + 1 / (i + 1) * (1 - s) > 0:
            m = i + 1
    maxi = 1 / m * (1 - u[:m].sum())
    return np.maximum(p + maxi, 0)
