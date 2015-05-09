import matplotlib.pyplot as plt


def plot(x, y, ind):
    '''Plots the original data with the peaks that were identified

    Parameters
    ----------
    x : array-like
        Data on the x-axis
    y : array-like
        Data on the y-axis
    ind : array-like
        Indexes of the identified peaks
    '''
    plt.plot(x, y, '--')
    plt.plot(x[ind], y[ind], 'r+', ms=5, mew=2,
             label='{} peaks'.format(len(ind)))
    plt.legend()
