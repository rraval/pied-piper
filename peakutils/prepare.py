'''Data preparation / preprocessing algorithms.'''

import numpy as np


def scale(x, new_range=(0., 1.), eps=1e-9):
    '''Changes the scale of an array

    Parameters
    ----------
    x : ndarray
        1D array to change the scale (remains unchanged)
    new_range : tuple (float, float)
        Desired range of the array
    eps: float
        Numerical precision, to detect degenerate cases (for example, when
        every value of *x* is equal)

    Returns
    -------
    ndarray
        Scaled array
    tuple (float, float)
        Previous data range, allowing a rescale to the old range
    '''
    assert new_range[1] >= new_range[0]
    range_ = (x.min(), x.max())

    if (range_[1] - range_[0]) < eps:
        mean = (new_range[0] + new_range[1]) / 2.0
        xp = np.full(x.shape, mean)
    else:
        xp = (x - range_[0])
        xp *= (new_range[1] - new_range[0]) / (range_[1] - range_[0])
        xp += new_range[0]

    return xp, range_
