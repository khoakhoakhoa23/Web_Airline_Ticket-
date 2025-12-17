import { createContext, useContext, useState, useCallback } from 'react';
import { flightService } from '../services/api';
import { toast } from 'react-toastify';

/**
 * Flight Search Context
 * 
 * Manages flight search state globally:
 * - Search parameters
 * - Search results
 * - Pagination
 * - Loading state
 */

const FlightSearchContext = createContext();

export const FlightSearchProvider = ({ children }) => {
  // Search parameters
  const [searchParams, setSearchParams] = useState(() => {
    // Try to restore from sessionStorage
    const saved = sessionStorage.getItem('flightSearchParams');
    return saved ? JSON.parse(saved) : {
      origin: '',
      destination: '',
      departureDate: '',
      passengers: 1,
      cabinClass: 'ECONOMY',
    };
  });

  // Search results
  const [searchResults, setSearchResults] = useState(() => {
    const saved = sessionStorage.getItem('flightSearchResults');
    return saved ? JSON.parse(saved) : [];
  });

  // Pagination
  const [pagination, setPagination] = useState(() => {
    const saved = sessionStorage.getItem('flightSearchPagination');
    return saved ? JSON.parse(saved) : {
      page: 0,
      size: 10,
      totalElements: 0,
      totalPages: 0,
    };
  });

  // Loading state
  const [loading, setLoading] = useState(false);

  // Filters
  const [filters, setFilters] = useState({
    minPrice: null,
    maxPrice: null,
    airline: '',
    sortBy: 'departTime', // departTime, price, duration
    sortOrder: 'asc',
  });

  /**
   * Update search parameters
   */
  const updateSearchParams = useCallback((params) => {
    setSearchParams(prev => {
      const updated = { ...prev, ...params };
      sessionStorage.setItem('flightSearchParams', JSON.stringify(updated));
      return updated;
    });
  }, []);

  /**
   * Search flights
   */
  const searchFlights = useCallback(async (params = null, page = 0) => {
    const searchData = params || searchParams;
    
    // Validate required fields
    if (!searchData.origin || !searchData.destination || !searchData.departureDate) {
      toast.error('Please fill in all required fields');
      return false;
    }

    setLoading(true);

    try {
      const response = await flightService.searchFlights({
        origin: searchData.origin.toUpperCase(),
        destination: searchData.destination.toUpperCase(),
        departureDate: searchData.departureDate,
        passengers: searchData.passengers || 1,
        cabinClass: searchData.cabinClass,
        minPrice: filters.minPrice,
        maxPrice: filters.maxPrice,
        airline: filters.airline,
        page,
        size: 10,
      });

      const data = response.data;
      const flights = data.content || [];

      if (page === 0) {
        // New search - replace results
        setSearchResults(flights);
        sessionStorage.setItem('flightSearchResults', JSON.stringify(flights));
      } else {
        // Load more - append results
        setSearchResults(prev => {
          const updated = [...prev, ...flights];
          sessionStorage.setItem('flightSearchResults', JSON.stringify(updated));
          return updated;
        });
      }

      setPagination({
        page: data.number || 0,
        size: data.size || 10,
        totalElements: data.totalElements || 0,
        totalPages: data.totalPages || 0,
      });

      sessionStorage.setItem('flightSearchPagination', JSON.stringify({
        page: data.number || 0,
        size: data.size || 10,
        totalElements: data.totalElements || 0,
        totalPages: data.totalPages || 0,
      }));

      if (flights.length === 0 && page === 0) {
        toast.info('No flights found for your search criteria');
      } else if (page === 0) {
        toast.success(`Found ${data.totalElements} flight${data.totalElements !== 1 ? 's' : ''}`);
      }

      return true;
    } catch (error) {
      console.error('Error searching flights:', error);
      const message = error.response?.data?.message || 'Failed to search flights. Please try again.';
      toast.error(message);
      return false;
    } finally {
      setLoading(false);
    }
  }, [searchParams, filters]);

  /**
   * Load more flights (pagination)
   */
  const loadMore = useCallback(() => {
    if (pagination.page + 1 < pagination.totalPages && !loading) {
      searchFlights(null, pagination.page + 1);
    }
  }, [pagination, loading, searchFlights]);

  /**
   * Update filters
   */
  const updateFilters = useCallback((newFilters) => {
    setFilters(prev => ({ ...prev, ...newFilters }));
  }, []);

  /**
   * Apply filters (re-search with filters)
   */
  const applyFilters = useCallback(() => {
    searchFlights(null, 0);
  }, [searchFlights]);

  /**
   * Clear filters
   */
  const clearFilters = useCallback(() => {
    setFilters({
      minPrice: null,
      maxPrice: null,
      airline: '',
      sortBy: 'departTime',
      sortOrder: 'asc',
    });
  }, []);

  /**
   * Clear search (reset)
   */
  const clearSearch = useCallback(() => {
    setSearchParams({
      origin: '',
      destination: '',
      departureDate: '',
      passengers: 1,
      cabinClass: 'ECONOMY',
    });
    setSearchResults([]);
    setPagination({
      page: 0,
      size: 10,
      totalElements: 0,
      totalPages: 0,
    });
    setFilters({
      minPrice: null,
      maxPrice: null,
      airline: '',
      sortBy: 'departTime',
      sortOrder: 'asc',
    });
    sessionStorage.removeItem('flightSearchParams');
    sessionStorage.removeItem('flightSearchResults');
    sessionStorage.removeItem('flightSearchPagination');
  }, []);

  const value = {
    // State
    searchParams,
    searchResults,
    pagination,
    loading,
    filters,
    
    // Actions
    updateSearchParams,
    searchFlights,
    loadMore,
    updateFilters,
    applyFilters,
    clearFilters,
    clearSearch,
  };

  return (
    <FlightSearchContext.Provider value={value}>
      {children}
    </FlightSearchContext.Provider>
  );
};

/**
 * Custom hook to use flight search context
 */
export const useFlightSearch = () => {
  const context = useContext(FlightSearchContext);
  if (!context) {
    throw new Error('useFlightSearch must be used within FlightSearchProvider');
  }
  return context;
};

